def KCI_CORE_BRANCH = System.getenv("KCI_CORE_BRANCH")
def KCI_CORE_URL = System.getenv("KCI_CORE_URL")
def KCI_STORAGE_URL = System.getenv("KCI_STORAGE_URL")
def KCI_API_URL = System.getenv("KCI_API_URL")
def DOCKER_BASE = System.getenv("DOCKER_BASE")
def CONFIG_LIST = System.getenv("CONFIG_LIST")
def KCI_LABS_LIST = System.getenv("KCI_LABS_LIST")
def KCI_CALLBACK_ID = System.getenv("KCI_CALLBACK_ID")

pipelineJob('kernel-tree-monitor') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch(KCI_CORE_BRANCH)
          remote {
            url(KCI_CORE_URL)
          }
        }
      }
      scriptPath('jenkins/monitor.jpl')
    }
  }
  parameters {
    stringParam('KCI_API_URL', KCI_API_URL, 'URL of the KernelCI back-end API.')
    stringParam('KCI_TOKEN_ID', 'api-token', 'Identifier of the KernelCI backend API token stored in Jenkins.')
    stringParam('KCI_STORAGE_URL', KCI_STORAGE_URL, 'URL of the KernelCI storage server.')
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('DOCKER_BASE', DOCKER_BASE, 'Dockerhub base address used for the build images.')
    stringParam('CONFIG_LIST', CONFIG_LIST, 'List of build configs to check instead of all the ones in build-configs.yaml.')
  }
}

pipelineJob('kernel-build-trigger') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch(KCI_CORE_BRANCH)
          remote {
            url(KCI_CORE_URL)
          }
        }
      }
      scriptPath('jenkins/build-trigger.jpl')
    }
  }
  configure { project ->
    project / 'properties' / 'org.jenkinsci.plugins.workflow.job.properties.DisableResumeJobProperty' {
      'switch'('on')
    }
  }
  logRotator {
    daysToKeep(7)
    numToKeep(200)
  }
  parameters {
    stringParam('BUILD_CONFIG', '', 'Name of the build configuration.')
    booleanParam('PUBLISH', true, 'Publish build results via the KernelCI backend API')
    booleanParam('EMAIL', true, 'Send build results via email')
    stringParam('LABS_WHITELIST', KCI_LABS_LIST, 'List of labs to include in the tests, all labs will be tested by default.')
    stringParam('KCI_TOKEN_ID', 'api-token', 'Identifier of the KernelCI backend API token stored in Jenkins.')
    stringParam('KCI_API_URL', KCI_API_URL, 'URL of the KernelCI Backend API')
    stringParam('KCI_STORAGE_URL', KCI_STORAGE_URL, 'URL of the KernelCI storage server.')
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('DOCKER_BASE', DOCKER_BASE, 'Dockerhub base address used for the build images.')
    booleanParam('ALLOW_REBUILD', false, 'Allow building the same revision again.')
  }
}

pipelineJob('kernel-build') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch(KCI_CORE_BRANCH)
          remote {
            url(KCI_CORE_URL)
          }
        }
      }
      scriptPath('jenkins/build.jpl')
    }
  }
  configure { project ->
    project / 'properties' / 'org.jenkinsci.plugins.workflow.job.properties.DisableResumeJobProperty' {
      'switch'('on')
    }
  }
  logRotator {
    daysToKeep(1)
  }
  parameters {
    stringParam('ARCH', '', 'CPU architecture as understood by the Linux kernel build system')
    stringParam('DEFCONFIG', '', 'Linux kernel defconfig to build')
    stringParam('SRC_TARBALL', '', 'URL of the kernel source tarball')
    stringParam('BUILD_CONFIG', '', 'Name of the build configuration')
    stringParam('GIT_DESCRIBE', '', "Output of 'git describe' at the revision of the snapshot")
    stringParam('GIT_DESCRIBE_VERBOSE', '', "Verbose output of 'git describe' at the revision of the snapshot")
    stringParam('COMMIT_ID', '', 'Git commit SHA1 at the revision of the snapshot')
    stringParam('BUILD_ENVIRONMENT', 'gcc-8', 'Name of the build environment')
    stringParam('NODE_LABEL', '', 'Label to use to choose a node on which to run this job')
    booleanParam('PUBLISH', true, 'Publish build results via the KernelCI backend API')
    booleanParam('EMAIL', true, 'Send build results via email')
    stringParam('KCI_API_URL', KCI_API_URL, 'URL of the KernelCI back-end API.')
    stringParam('KCI_TOKEN_ID', 'api-token', 'Identifier of the KernelCI backend API token stored in Jenkins.')
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('PARALLEL_BUILDS', '4', 'Number of kernel builds to run in parallel')
    stringParam('DOCKER_BASE', DOCKER_BASE, 'Dockerhub base address used for the build images.')
  }
}

job('kernel-arch-complete') {
  label('build-complete')
  logRotator {
    daysToKeep(7)
    numToKeep(30)
  }
  wrappers {
      preBuildCleanup()
      credentialsBinding {
          string('EMAIL_AUTH_TOKEN', 'api-token')
      }
  }
  parameters {
    stringParam('TREE_NAME', '', 'Name of the tree to be tested')
    stringParam('GIT_DESCRIBE', '', "Output of 'git describe' at the revision of the snapshot")
    stringParam('ARCH', '', 'CPU architecture as understood by the Linux kernel build system')
    stringParam('RESULTS', '', '')
    booleanParam('PUBLISH', false, 'Publish build results via the KernelCI backend API')
    booleanParam('EMAIL', true, 'Send build results via email')
    stringParam('BRANCH', '', '')
    stringParam('API', KCI_API_URL, 'URL of the KernelCI back-end API.')
  }
  steps {
    shell("(rm -rf kernelci-core; git clone --depth 1 -b " + KCI_CORE_BRANCH + " " + KCI_CORE_URL + """ kernelci-core)

./kernelci-core/jenkins/kernel-arch-complete.sh""")
  }
}

pipelineJob('test-runner') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch(KCI_CORE_BRANCH)
          remote {
            url(KCI_CORE_URL)
          }
        }
      }
      scriptPath('jenkins/test-runner.jpl')
    }
  }
  configure { project ->
    project / 'properties' / 'org.jenkinsci.plugins.workflow.job.properties.DisableResumeJobProperty' {
      'switch'('on')
    }
  }
  logRotator {
    daysToKeep(7)
    numToKeep(200)
  }
  parameters {
    stringParam('LABS', '', 'Names of the labs where to submit tests')
    stringParam('TRIGGER_JOB_NAME', 'kernel-build-trigger', 'Name of the parent trigger job')
    stringParam('TRIGGER_JOB_NUMBER', '', 'Number of the parent trigger job')
    stringParam('KCI_STORAGE_URL', KCI_STORAGE_URL, 'URL of the KernelCI storage server.')
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('DOCKER_BASE', DOCKER_BASE, 'Dockerhub base address used for the build images.')
    stringParam('BUILD_JOB_NAME', 'kernelci-build', 'Name of the job that built the kernel')
    stringParam('BUILD_JOB_NUMBER', '', 'Number of the job that built the kernel')
    stringParam('CALLBACK_ID', KCI_CALLBACK_ID, 'Identifier of the callback to look up an authentication token')
    stringParam('CALLBACK_URL', KCI_API_URL, 'Base URL where to send the callbacks')
  }
}
