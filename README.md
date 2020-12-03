Jenkins Docker container with Config-as-Code
============================================

This repository contains the [Jenkins
Config-as-Code](https://www.jenkins.io/projects/jcasc/) (JCasC) YAML needed to
create a pre-configured Jenkins Docker container, and the `job-dsl` Groovy code
for generating the KernelCI jobs.  There are some sample `docker-compose`
environment files with variables used for production, staging, and development.

## Prerequisites

This Jenkins project is used to run a full KernelCI pipeline automatically.  It
needs at least a
[`kernelci-backend`](https://github.com/kernelci/kernelci-backend) instance to
be able to publish kernel builds, and typically at least one
[LAVA](https://www.lavasoftware.org/) lab instance to run tests.  Installing
those things is not covered here, please refer to their corresponding
documentation.

## Configuration

* Copy the `env` file to `.env` and edit it with your own settings
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

## Usage

Then the container can be used using regular `docker-compose` commands.  Here
are a few examples:
* `docker-compose up --build`
  * **action**: start the container in the foreground
  * **notes**: useful when testing the configuration

* `docker-compose up --build -d`
  * **action**: start the container in the background
  * **notes**: typical usage to keep the service running

* `docker-compose stop`
  * **action**: stop the container
  * **notes**: useful when editing the configuration

* `docker-compose down`
  * **action**: destroy the container
  * **notes**: WARNING: any jobs data or manual Jenkins configuration will be
    lost

* `docker-compose logs -f jenkins`
  * **action**: read the Jenkins logs
  * **notes**: useful for monitoring activity

* `docker-compose exec jenkins bash`
  * **action**: start an interactive shell in the running container
  * **notes**: useful to debug problems by accessing files directly

Initially a single job is created, this is the DSL seed job which can be used
to create all the other ones defined in [`jobs.groovy`](jobs.groovy).  Open the
Jenkins web UI and start this job to see the other ones listed below appear on
the Jenkins instance.

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
