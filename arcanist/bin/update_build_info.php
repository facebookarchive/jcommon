#!/usr/bin/env php
<?php

// Copyright 2004-present Facebook. All Rights Reserved.

/**
 * This program is used to Update the build and test results on the
 * diff page and is meant to be run from within a Jenkins Job.
 *
 * Some of the information this program needs is read from the
 * environment instead of command line options. Specifically,
 * the BUILD_TYPE and GIT_BRANCH are expected to be set.
 *
 * This program can be run in two modes:
 *
 *  * As a pipe to read the test output.
 *    Use this mode when running the tests so that unit failures
 *    are posted as they are found. From the jenkins job, do something
 *    like:
 *    new-mysqldev.sh 56test --skip-test-list=test.skip | update_build_info.php
 *
 *  * As a single invocation to set the build status.
 *    Use this mode when the build portion fails. Use the
 *    --status= and --message= options to update the diff page.
 *
 *    From the jenkins job, do:
 *    new-mysqldev.sh clean && \
 *         update_build_info.php --status=pass --message="Good build"
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

// When the build fails to compile, just send a 'build failed' message
function update_buildstatus($conduit, $diff_id, $status, $message) {

  $conduit->callMethodSynchronous(
        'differential.updateunitresults',
        array(
          'diff_id' => $diff_id,
          'file'    => 'jcommon_build',
          'name'    => 'jcommon_build',
          'result'  => $status,
          'message' => $message,
        ));
}

/**
 * Send results to phabricator.
 */
function update_unitresults($conduit, $diff_id, $results) {

  // possible results: 'pass', 'fail', 'skip', 'broken', 'skip', 'unsound'
  foreach ($results as $result) {
    print("{$diff_id}\n");
    print_r($result);
    $conduit->callMethodSynchronous(
          'differential.updateunitresults',
          array(
            'diff_id'  => $diff_id,
            'file'     => $result['file'],
            'name'     => $result['name'],
            'result'   => $result['status'],
            'message'  => $result['msg'],
            'coverage' => $result['coverage'],
          ));
  }
}

function test($conduit, $diff_id) {
  $results = mock_test_results();
  update_unitresults($conduit, $diff_id, $results);
}

/* For the files that were changed, get the coverage info for each
 * file and pass back to phabricator
*/
function get_coverage_info() {
  $results = array();
  $gitcmd = 'git show --pretty="format:" --name-only HEAD';
  $git_future = new ExecFuture($gitcmd);
  list($files) = $git_future->resolvex();

  foreach (explode("\n", $files) as $file) {
    if (!preg_match('/\.java$/', $file)) {
      continue;
    }
    array_push($results,
                  array('file'     => $file,
                        'name'     => $file,
                        'status'   => "pass",
                        'msg'      => "",
                        'coverage' => "",
                  )
    );
  }
  return $results;
}

// Command line option specification
$specs = array(
  array(
    'name' => 'status',
    'param' => 'value',
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

// If we were given a status and message, update the diff and exit
if ($status = $args->getArg('status')) {
  $message = $args->getArg('message') ?: "";
  update_buildstatus($conduit, $diff_id, $status, $message);
  exit(0);
}

$results = get_coverage_info();

update_unitresults($conduit, $diff_id, $results);
