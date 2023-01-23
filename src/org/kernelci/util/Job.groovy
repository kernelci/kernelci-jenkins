/*
  Copyright (C) 2018 Collabora Limited
  Author: Guillaume Tucker <guillaume.tucker@collabora.com>

  This module is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.

  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
  details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/


package org.kernelci.util;

def addStrParams(params, str_params) {
    for (p in str_params) {
        params.push(
            [$class: "StringParameterValue", name: p.key, value: p.value])
    }
}

def addBoolParams(params, bool_params) {
    for (p in bool_params) {
        params.push(
            [$class: "BooleanParameterValue", name: p.key, value: p.value])
    }
}

def cloneKciCore(path, url, branch) {
    sh(script: "rm -rf ${path}")
    dir("${path}") {
        git(url: url,
            branch: branch,
            poll: false)
    }
}

def dockerImageName(build_env, kernel_arch) {
    def image_name = build_env

    def build_env_raw = sh(
        script: """
kci_build \
  show_build_env \
  --build-env=${build_env} \
  --arch=${kernel_arch} \
""", returnStdout: true).trim()
    def build_env_data = build_env_raw.split('\n').toList()
    def cc_arch = build_env_data[3]

    /* Some architecture builds include other arch */
    if (cc_arch == 'x86_64')
        cc_arch = "x86"
    if (cc_arch == 'riscv')
        cc_arch = "riscv64"

    if (cc_arch == 'sparc') /* No kselftest variant for sparc */
        image_name = "${build_env}:${cc_arch}-kernelci"
    else if (cc_arch)
        image_name = "${build_env}:${cc_arch}-kselftest-kernelci"
    else
        image_name = "${build_env}:kselftest-kernelci"

    return image_name
}

def dockerPullWithRetry(image_name, retries=10, sleep_time=1) {
    def image = docker.image(image_name)
    def pulled = false

    while (!pulled) {
        try {
            image.pull()
            pulled = true
        }
        catch (Exception e) {
            if (!retries) {
                throw e
            }
            echo("""Docker pull failed, retry count ${retries}: ${e.toString()}""")
            sleep sleep_time
            retries -= 1
            sleep_time = sleep_time * 2
        }
    }

    return image
}
