Jenkins Docker container with Config-as-Code
============================================

This repository contains the [Jenkins
Config-as-Code](https://www.jenkins.io/projects/jcasc/) (JCasC) YAML needed to
create a pre-configured Jenkins Docker container, and the `job-dsl` Groovy code
for generating the KernelCI jobs.  There are some sample `docker-compose`
environment files with variables used for production, staging, and development.

Usage
=====

* Choose a `env-*` file and copy it to `.env`, or create your own.
* Add any extra plugins (`github-auth`, `openstack` etc) to the
  [`plugins-extra.txt`](plugins-extra.txt) file and they will be included in
  the build.
* Add your LAVA lab credentials in [`config/secrets.yaml`](config/secrets.yaml)
* Any CasC YAML files that exist in the `config` directory will also be
  loaded, so you can create your own.
* Jenkins nodes should be configured in `config/nodes.yaml` (you will have to
  create this).
* Add any extra parameters you've used in your config to `.env`.
* Edit the `ADMIN_PASSWORD` in `.env`.

Execute `docker-compose up --build` and Jenkins should launch.

Initially a single job is created, this is the DSL seed job which should create
all the other ones defined in [`jobs.groovy`](jobs.groovy).

## Jenkins jobs

All the automated jobs on kernelci.org are run in Jenkins.  Some legacy scripts
are still being used in "freestyle" projects but they are gradually being
replaced with Pipeline jobs.  Each Pipeline job has a `.jpl` file located in
the `jenkins` directory:

* [`jenkins/monitor.jpl`](https://github.com/kernelci/kernelci-jenkins/tree/master/jobs/monitor.jpl) to monitor kernel branches and detect new revisions
* [`jenkins/build-trigger.jpl`](https://github.com/kernelci/kernelci-jenkins/tree/master/jobs/build-trigger.jpl) to trigger all the builds for a kernel revision
* [`jenkins/build.jpl`](https://github.com/kernelci/kernelci-jenkins/tree/master/jobs/build.jpl) to build a single kernel
* [`jenkins/test-runner.jpl`](https://github.com/kernelci/kernelci-jenkins/tree/master/jobs/test-runner.jpl) to run tests for a single kernel build
* [`jenkins/bisect.jpl`](https://github.com/kernelci/kernelci-jenkins/tree/master/jobs/bisect.jpl) to run automated test bisections
* [`jenkins/rootfs-trigger.jpl`](https://github.com/kernelci/kernelci-jenkins/tree/master/jobs/rootfs-trigger.jpl) to trigger a series of rootfs image builds
* [`jenkins/rootfs-builder.jpl`](https://github.com/kernelci/kernelci-jenkins/tree/master/jobs/rootfs-builder.jpl) to build a single rootfs image

In addition to the job files, there are also some common library files located
in the
[`src/org/kernelci`](https://github.com/kernelci/kernelci-core/tree/master/src/org/kernelci)
directory.
