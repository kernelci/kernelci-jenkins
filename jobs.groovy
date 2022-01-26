def KCI_CORE_BRANCH = System.getenv("KCI_CORE_BRANCH")
def KCI_CORE_URL = System.getenv("KCI_CORE_URL")
def KCI_STORAGE_URL = System.getenv("KCI_STORAGE_URL")
def KCI_DB_CONFIG = System.getenv("KCI_DB_CONFIG")
def KCI_API_URL = System.getenv("KCI_API_URL")
def KCI_API_TOKEN_ID = System.getenv("KCI_API_TOKEN_ID")
def KCI_DOCKER_BASE = System.getenv("KCI_DOCKER_BASE")
def KCI_CONFIG_LIST = System.getenv("KCI_CONFIG_LIST")
def KCI_LABS_LIST = System.getenv("KCI_LABS_LIST")
def KCI_CALLBACK_ID = System.getenv("KCI_CALLBACK_ID")
def KCI_BISECTION_CALLBACK_ID = System.getenv("KCI_BISECTION_CALLBACK_ID")
def KCI_BISECTION_EMAIL_RECIPIENTS = System.getenv("KCI_BISECTION_EMAIL_RECIPIENTS")
def KCI_BISECTION_TREES_WHITELIST = System.getenv("KCI_BISECTION_TREES_WHITELIST")
def KCI_BISECTION_LABS_WHITELIST = System.getenv("KCI_BISECTION_LABS_WHITELIST")
def KCI_MONITOR_CRON = System.getenv("KCI_MONITOR_CRON")
def KCI_JENKINS_BRANCH = System.getenv("KCI_JENKINS_BRANCH")
def KCI_JENKINS_URL = System.getenv("KCI_JENKINS_URL")

pipelineJob('kernel-tree-monitor') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch(KCI_JENKINS_BRANCH)
          remote {
            url(KCI_JENKINS_URL)
          }
        }
      }
      scriptPath('jobs/monitor.jpl')
    }
    if (KCI_MONITOR_CRON) {
      triggers {
        cron(KCI_MONITOR_CRON)
      }
    }
  }
  logRotator {
    daysToKeep(7)
    numToKeep(200)
  }
  parameters {
    stringParam('KCI_API_URL', KCI_API_URL, 'URL of the KernelCI back-end API.')
    stringParam('KCI_API_TOKEN_ID', KCI_API_TOKEN_ID, 'Identifier of the KernelCI backend API token stored in Jenkins.')
    stringParam('KCI_STORAGE_URL', KCI_STORAGE_URL, 'URL of the KernelCI storage server.')
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('DOCKER_BASE', KCI_DOCKER_BASE, 'Dockerhub base address used for the build images.')
    stringParam('CONFIG_LIST', KCI_CONFIG_LIST, 'List of build configs to check instead of all the ones in build-configs.yaml.')
  }
}

pipelineJob('kernel-build-trigger') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch(KCI_JENKINS_BRANCH)
          remote {
            url(KCI_JENKINS_URL)
          }
        }
      }
      scriptPath('jobs/build-trigger.jpl')
    }
  }
  configure { project ->
    project / 'properties' / 'org.jenkinsci.plugins.workflow.job.properties.DisableResumeJobProperty' {
      'switch'('on')
    }
  }
  logRotator {
    daysToKeep(7)
    numToKeep(48)
  }
  parameters {
    stringParam('BUILD_CONFIG', '', 'Name of the build configuration.')
    booleanParam('PUBLISH', true, 'Publish build results via the KernelCI backend API')
    booleanParam('EMAIL', true, 'Send build results via email')
    stringParam('LABS_WHITELIST', KCI_LABS_LIST, 'List of labs to include in the tests, all labs will be tested by default.')
    stringParam('KCI_API_TOKEN_ID', KCI_API_TOKEN_ID, 'Identifier of the KernelCI backend API token stored in Jenkins.')
    stringParam('KCI_API_URL', KCI_API_URL, 'URL of the KernelCI Backend API')
    stringParam('KCI_STORAGE_URL', KCI_STORAGE_URL, 'URL of the KernelCI storage server.')
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('DOCKER_BASE', KCI_DOCKER_BASE, 'Dockerhub base address used for the build images.')
    booleanParam('ALLOW_REBUILD', false, 'Allow building the same revision again.')
  }
}

pipelineJob('kernel-build') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch(KCI_JENKINS_BRANCH)
          remote {
            url(KCI_JENKINS_URL)
          }
        }
      }
      scriptPath('jobs/build.jpl')
    }
  }
  configure { project ->
    project / 'properties' / 'org.jenkinsci.plugins.workflow.job.properties.DisableResumeJobProperty' {
      'switch'('on')
    }
  }
  logRotator {
    daysToKeep(2)
    numToKeep(4096)
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
    stringParam('KCI_DB_CONFIG', KCI_DB_CONFIG, 'Value to use with the --db-config argument')
    stringParam('KCI_API_URL', KCI_API_URL, 'URL of the KernelCI back-end API.')
    stringParam('KCI_API_TOKEN_ID', KCI_API_TOKEN_ID, 'Identifier of the KernelCI backend API token stored in Jenkins.')
    stringParam('KCI_STORAGE_URL', KCI_STORAGE_URL, 'URL of the KernelCI storage server.')
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('PARALLEL_BUILDS', '4', 'Number of kernel builds to run in parallel')
    stringParam('DOCKER_BASE', KCI_DOCKER_BASE, 'Dockerhub base address used for the build images.')
  }
}

job('kernel-arch-complete') {
  label('build-complete')
  logRotator {
    daysToKeep(7)
    numToKeep(100)
  }
  wrappers {
      preBuildCleanup()
      credentialsBinding {
          string('EMAIL_AUTH_TOKEN', KCI_API_TOKEN_ID)
      }
  }
  parameters {
    stringParam('TREE_NAME', '', 'Name of the tree to be tested')
    stringParam('GIT_DESCRIBE', '', "Output of 'git describe' at the revision of the snapshot")
    booleanParam('PUBLISH', true, 'Publish build results via the KernelCI backend API')
    booleanParam('EMAIL', true, 'Send build results via email')
    stringParam('BRANCH', '', '')
    stringParam('API', KCI_API_URL, 'URL of the KernelCI backend API.')
  }
  steps {
    shell("""
#!/bin/bash

set -e

rm -rf kernelci-jenkins
git clone --depth 1 -b ${KCI_JENKINS_BRANCH} ${KCI_JENKINS_URL}
./kernelci-jenkins/scripts/kernel-arch-complete.sh
""")
  }
}

pipelineJob('test-runner') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch(KCI_JENKINS_BRANCH)
          remote {
            url(KCI_JENKINS_URL)
          }
        }
      }
      scriptPath('jobs/test-runner.jpl')
    }
  }
  configure { project ->
    project / 'properties' / 'org.jenkinsci.plugins.workflow.job.properties.DisableResumeJobProperty' {
      'switch'('on')
    }
  }
  logRotator {
    daysToKeep(1)
    numToKeep(1024)
  }
  parameters {
    stringParam('LABS', '', 'Names of the labs where to submit tests')
    stringParam('TRIGGER_JOB_NAME', 'kernel-build-trigger', 'Name of the parent trigger job')
    stringParam('TRIGGER_JOB_NUMBER', '', 'Number of the parent trigger job')
    stringParam('KCI_STORAGE_URL', KCI_STORAGE_URL, 'URL of the KernelCI storage server.')
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('DOCKER_BASE', KCI_DOCKER_BASE, 'Dockerhub base address used for the build images.')
    stringParam('BUILD_JOB_NAME', 'kernel-build', 'Name of the job that built the kernel')
    stringParam('BUILD_JOB_NUMBER', '', 'Number of the job that built the kernel')
    stringParam('CALLBACK_ID', KCI_CALLBACK_ID, 'Identifier of the callback to look up an authentication token')
    stringParam('CALLBACK_URL', KCI_API_URL, 'Base URL where to send the callbacks')
  }
}

pipelineJob('rootfs-build-trigger') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch('main')
          remote {
            url(KCI_JENKINS_URL)
          }
        }
      }
      scriptPath('jobs/rootfs-trigger.jpl')
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
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('DOCKER_BASE', KCI_DOCKER_BASE, 'Dockerhub base address used for the rootfs build images.')
    stringParam('ROOTFS_CONFIG','','Name of the rootfs configuration, all rootfs will be built by default.')
    stringParam('ROOTFS_ARCH','','Name of the rootfs arch config, all given arch will be built by default.')
  }
}

pipelineJob('rootfs-builder') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch('main')
          remote {
            url(KCI_JENKINS_URL)
          }
        }
      }
      scriptPath('jobs/rootfs-builder.jpl')
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
    stringParam('KCI_API_TOKEN_ID', KCI_API_TOKEN_ID, 'Identifier of the KernelCI backend API token stored in Jenkins.')
    stringParam('KCI_API_URL', KCI_API_URL, 'URL of the KernelCI Backend API')
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('DOCKER_BASE', KCI_DOCKER_BASE, 'Dockerhub base address used for the rootfs build images.')
    stringParam('ROOTFS_CONFIG','','Name of the rootfs configuration, all rootfs will be built by default.')
    stringParam('ROOTFS_ARCH','','Name of the rootfs arch config, all given arch will be built by default.')
    stringParam('ROOTFS_TYPE','debos','Name of the rootfs type which can be debos or buildroot.')
    stringParam('PIPELINE_VERSION','','Unique string identifier for the series of rootfs build jobs.')
  }
}

pipelineJob('lava-bisection') {
  definition {
    cpsScm {
      lightweight(true)
      scm {
        git {
          branch(KCI_JENKINS_BRANCH)
          remote {
            url(KCI_JENKINS_URL)
          }
        }
      }
      scriptPath('jobs/bisect.jpl')
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
    stringParam('KERNEL_URL', '', 'URL of the kernel Git repository')
    stringParam('KERNEL_BRANCH', '', 'Name of the branch to bisect in the kernel Git repository')
    stringParam('KERNEL_TREE', '', 'Name of the kernel Git repository (tree)')
    stringParam('KERNEL_NAME', '', 'Identifier of the kernel (typically `git describe`)')
    stringParam('GOOD_COMMIT', '', 'Good known Git revision (SHA1 or any valid reference)')
    stringParam('BAD_COMMIT', '', 'Bad known Git revision (SHA1 or any valid reference)')
    stringParam('REF_KERNEL_URL',
                'git://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git',
                'URL of the reference kernel Git repository used to find merge bases')
    stringParam('REF_KERNEL_BRANCH', 'master', 'Name of the branch from the reference kernel Git repository')
    stringParam('REF_KERNEL_TREE', 'mainline', 'Name of the reference kernel Git repository')
    stringParam('ARCH', '', 'CPU architecture as understood by the Linux kernel build system')
    stringParam('DEFCONFIG', 'defconfig', 'Name of the Linux kernel defconfig')
    stringParam('TARGET', '', 'Name of the device type to test (typically LAVA device type name)')
    stringParam('BUILD_ENVIRONMENT', '', 'Name of the build environment')
    stringParam('LAB', '', 'Name of the lab in which to run the bisection tests')
    stringParam('TEST_PLAN_VARIANT', '', 'Name of the KernelCI test plan variant (e.g. baseline_qemu)')
    stringParam('TEST_CASE', '', 'Test case path in dotted syntax (e.g. baseline.dmesg.crit)')
    stringParam('TEST_RUNS', '1', 'Number of LAVA jobs to run before considering pass or fail.')

    stringParam('LAVA_CALLBACK', KCI_BISECTION_CALLBACK_ID, 'Description of the LAVA auth token to look up and use in LAVA callbacks')
    stringParam('EMAIL_RECIPIENTS', KCI_BISECTION_EMAIL_RECIPIENTS, 'List of recipients for all emails generated by this job')
    stringParam('LABS_WHITELIST', KCI_BISECTION_LABS_WHITELIST, 'If defined, jobs will abort if the LAB is not on that list.')
    stringParam('TREES_WHITELIST', KCI_BISECTION_TREES_WHITELIST, 'If defined, jobs will abort if the KERNEL_TREE is not on that list.')

    stringParam('KCI_API_URL', KCI_API_URL, 'URL of the KernelCI back-end API.')
    stringParam('KCI_API_TOKEN_ID', KCI_API_TOKEN_ID, 'Identifier of the KernelCI backend API token stored in Jenkins.')
    stringParam('KCI_STORAGE_URL', KCI_STORAGE_URL, 'URL of the KernelCI storage server.')
    stringParam('KCI_DB_CONFIG', KCI_DB_CONFIG, 'Value to use with the --db-config argument')
    stringParam('KCI_CORE_URL', KCI_CORE_URL, 'URL of the kernelci-core repository.')
    stringParam('KCI_CORE_BRANCH', KCI_CORE_BRANCH, 'Name of the branch to use in the kernelci-core repository.')
    stringParam('DOCKER_BASE', KCI_DOCKER_BASE, 'Dockerhub base address used for the build images.')
  }
}

matrixJob('buildroot') {
  logRotator {
    daysToKeep(7)
    numToKeep(100)
  }
  scm {
    git {
      remote {
        url('https://github.com/kernelci/buildroot.git')
      }
      branch('kernelci/latest')
    }
  }
  wrappers {
      credentialsBinding {
          string('API_TOKEN', KCI_API_TOKEN_ID)
      }
  }
  parameters {
    stringParam('API', KCI_API_URL, 'URL of the KernelCI backend API.')
    stringParam('STORAGE', KCI_STORAGE_URL, 'URL of the KernelCI storage server.')
  }
  axes {
      label('label', 'buildroot')
      text('ARCH', [
          'arc', 'armeb', 'armel', 'arm64', 'arm64be', 'mipsel', 'riscv', 'x86'
      ])
      text('FRAG', 'baseline')
  }
  steps {
    shell("""
#!/bin/bash

set -e

rm -rf kernelci-jenkins
git clone --depth 1 -b ${KCI_JENKINS_BRANCH} ${KCI_JENKINS_URL}
export PATH=\$PWD/kernelci-jenkins/scripts:\$PATH
rm -rf output
./kernelci-jenkins/scripts/buildroot-builder.sh \$ARCH \$FRAG
""")
    }
}

matrixJob('buildroot-staging') {
  logRotator {
    daysToKeep(7)
    numToKeep(100)
  }
  scm {
    git {
      remote {
        url('https://github.com/kernelci/buildroot.git')
      }
      branch('staging.kernelci.org')
    }
  }
  wrappers {
      credentialsBinding {
          string('API_TOKEN', KCI_API_TOKEN_ID)
      }
  }
  parameters {
    stringParam('API', KCI_API_URL, 'URL of the KernelCI backend API.')
    stringParam('STORAGE', KCI_STORAGE_URL, 'URL of the KernelCI storage server.')
  }
  axes {
      label('label', 'buildroot')
      text('ARCH', [
          'arc', 'armeb', 'armel', 'arm64', 'arm64be', 'mipsel', 'riscv', 'x86'
      ])
      text('FRAG', 'baseline')
  }
  steps {
    shell("""
#!/bin/bash

set -e

rm -rf kernelci-jenkins
git clone --depth 1 -b staging.kernelci.org ${KCI_JENKINS_URL}
export PATH=\$PWD/kernelci-jenkins/scripts:\$PATH
rm -rf output
./kernelci-jenkins/scripts/buildroot-builder.sh \$ARCH \$FRAG
""")
    }
}
