#!/usr/bin/env php
<?php

// Copyright 2004-present Facebook. All Rights Reserved.

/**
 * This program is used to Update the build and test results on the
 * diff page and is meant to be run from within a Jenkins Job.
 *
 * Some of the information this program needs is read from the
 * environment instead of command line options. Specifically,
 * GIT_BRANCH is expected to be set.
 *
 * This program is meant to be run after coverting the Cobertura
 * coverage files into a json format that has the coverage info in
 * a format that is compatible with phabricator.
 *
 * For now, this program assumes there is a single 'coverage.json' file
 * in the diretory where the program is run.
 *
 * TODO:
 * - Find coverage.json files as there might be more than one
 * - alternatively, take the json file(s) as command line arguments
 * - Work with emma code coverage tool
 * - aggregate the unit test info and update the diff with the coverage
 *   based on the tests that ran.
 */

require_once '/home/engshare/devtools/arcanist/scripts/__init_script__.php';

/**
 * Configure the conduit call to phabricator
 * The 'svcscm' user is the user that runs the Jenkins builds as well
 * as has access to the git/svn repositories.
 */
function get_conduit() {

  $endpoint = 'https://phabricator.fb.com/api/';
  $conduit = new ConduitClient($endpoint);

  $user = 'svcscm';
  $cert = 'nqvznivfyrbi56s65vur26zucxfgto6wfqsqqp2ihwtybzurcrv53cxt7ikpthu26'.
          'dcrdgeg55nxaqu6ebagch5e2el752mygos6wtjcsf5b6mwvcq4vzjao4hyiknakmg'.
          'vf2evfazhivbnce6qih3wgn7r2rwg64i2ki5wayl6mvlmuaktu4meaqayerzdzifr'.
          'mejcabzfdjb6lq6d2mfzontnn2xndatmhfctyg5fick3cugiebmasvknnvrt';


  $response = $conduit->callMethodSynchronous(
    'conduit.connect',
    array(
      'client'            => 'Continuous Builder Client',
      'clientVersion'     => '1.0',
      'clientDescription' => 'Test updater for Jenkins',
      'user'              => $user,
      'certificate'       => $cert,
    ));

  return $conduit;
}

function get_diffid() {
  $git_branch = getenv('GIT_BRANCH');

  // Exit here if GIT_BRANCH isn't set.
  if (! $git_branch) {
    echo "Error: GIT_BRANCH was not set. Are you running this outside\n".
         "of a Jenkins build?\n";
    exit(1);
  }

  // The branch should look like refs/autobuilds/<digits>
  $diff_id = array_pop(explode("/", $git_branch));

  if (!preg_match('/\d+/', $diff_id)) {
    echo "Error: the diff_id ($diff_id) was not a valid diff.\n";
    exit(1);
  }
  return $diff_id;
}

/*
 * For the files that were changed, get the coverage info for each
 * file. The structure that gets passed back to phabricator will
 * look like:
 *
 * {"name":"TaskTestCase","result":"pass",
 * "coverage":{"lib/foo.php":"NNNNNNNNU", "lib.bar.php":"CNCNCNCU"}}
 *
 */
function get_coverage_info() {
  $gitcmd = 'git show --pretty="format:" --name-only HEAD';
  $git_future = new ExecFuture($gitcmd);
  list($files) = $git_future->resolvex();
  $results = array();

  // TODO: need to find all of the coverage.json files
  $coverage_file = 'coverage.json';
  if (! file_exists($coverage_file) || ! is_readable($coverage_file)) {
    echo "$coverage_file does not exist";
    return array();
  }

  $json_string = file_get_contents($coverage_file);
  $json = json_decode($json_string);

  foreach (explode("\n", $files) as $file) {
    if (!preg_match('/\.java$/', $file)) {
      continue;
    }
    $results[$file] = isset($json->{$file}) ? $json->{$file} : '';
  }
  return $results;
}

function get_test_results($test) {
  $result = array(
    'name'     => $test,
    'status'   => 'pass',
    'message'  => '',
    'coverage' => get_coverage_info($test)
  );
  return array($result);
}

// When the build fails to compile, just send a 'build failed' message
function update_buildstatus($conduit, $diff_id, $name, $status, $message) {

  $conduit->callMethodSynchronous(
        'differential.updateunitresults',
        array(
          'diff_id' => $diff_id,
          'file'    => $name,
          'name'    => $name,
          'result'  => $status,
          'message' => $message,
        )
  );
}

/**
 * Send results to phabricator.
 */
function update_unitresults($conduit, $diff_id, $name) {

  $tests = get_test_results($name);

  // possible results: 'pass', 'fail', 'skip', 'broken', 'skip', 'unsound'
  foreach ($tests as $test) {
    $conduit->callMethodSynchronous(
          'differential.updateunitresults',
          array(
            'diff_id'  => $diff_id,
            'name'     => $test['name'],
            'result'   => $test['status'],
            'message'  => $test['message'],
            'coverage' => $test['coverage'],
          ));
  };
}

// Command line option specification
$specs = array(
  array(
    'name' => 'status',
    'param' => 'value',
  ),
  array(
    'name' => 'name',
    'param' => 'value',
    'help'  => 'Name of the build',
  ),
  array(
    'name' => 'message',
    'param' => 'value',
  ),
);

$args = new PhutilArgumentParser($argv);
$args->parseFull($specs);

$diff_id = get_diffid();
$conduit = get_conduit();
if (! $name = $args->getArg('name')) {
  echo "Error: Missing 'name' argument.";
  exit(1);
}

// If we were given a status and message, update the diff and exit
if ($status = $args->getArg('status')) {
  $message = $args->getArg('message') ?: "";
  update_buildstatus($conduit, $diff_id, $name, $status, $message);
  exit(0);
}

update_unitresults($conduit, $diff_id, $name);
