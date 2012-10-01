<?php
// Copyright 2004-present Facebook. All Rights Reserved.

class FacebookJcommonUnitTestEngine extends ArcanistBaseUnitTestEngine {

  public function run() {
    // If we are running asynchronously, mark all tests as postponed
    // and return those results.  Otherwise, run the tests and collect
    // the actual results.
    if ($this->getEnableAsyncTests()) {
      $results = array();
      $result = new ArcanistUnitTestResult();
      $result->setName("jcommon_build");
      $result->setResult(ArcanistUnitTestResult::RESULT_POSTPONED);
      $results[] = $result;
      return $results;
    } else {
      $server = new FacebookBuildServer();
      $server->startProjectBuilds(false);
      return array();
    }
  }

}
