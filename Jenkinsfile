#!/usr/bin/env groovy

import java.util.Collections

// Valid Jenkins versions for test
def testJenkinsVersions = [ '2.204.1', '2.204.6', '2.222.4', '2.235.1', '2.243' ]
Collections.shuffle(testJenkinsVersions)

// Test plugin compatibility to subset of Jenkins versions
subsetConfiguration = [ [ jdk: '8',  platform: 'windows', jenkins: '2.204.1' ],
                        [ jdk: '8',  platform: 'linux',   jenkins: '2.204.6' ],
                        [ jdk: '11', platform: 'linux',   jenkins: '2.235.1' ]
                      ]

buildPlugin(configurations: subsetConfiguration, failFast: false)
