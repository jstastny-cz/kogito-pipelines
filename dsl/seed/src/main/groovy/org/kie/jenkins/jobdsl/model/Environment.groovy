package org.kie.jenkins.jobdsl.model

import org.kie.jenkins.jobdsl.Utils

/*
* Environment represents the environment is which a job should run
*
* Each environment can contain default env variables which will be added automatically to the job.
* For this `getDefaultEnvVarsClosure` should be initialized.
*
* Also an environment can be optional.
* So an environment can be disabled and the method `isActiveClosure` should be initialized else it returns `true` by default.
*
*/
enum Environment {

    DEFAULT(
        optional: false,
    ),
    SONARCLOUD(
        optional: true,
        isActiveClosure: { script -> Utils.isMainBranch(script) },
    ),
    NATIVE(
        optional: true,
        isActiveClosure: { script -> Utils.isNativeEnvironmentEnabled(script) },
        getDefaultEnvVarsClosure: { script ->
            [
                NATIVE: 'true',
                ADDITIONAL_TIMEOUT: 720
            ]
        }
    ),
    MANDREL(
        optional: true,
        isActiveClosure: { script -> Utils.isMandrelEnvironmentEnabled(script) },
        getDefaultEnvVarsClosure: { script ->
            [
                NATIVE: 'true',
                NATIVE_BUILDER_IMAGE: Utils.getMandrelEnvironmentBuilderImage(script),
                ADDITIONAL_TIMEOUT: 720
            ]
        }
    ),
    QUARKUS_MAIN(
        optional: true,
        isActiveClosure: { script -> Utils.isQuarkusMainEnvironmentEnabled(script) },
        getDefaultEnvVarsClosure: { script -> [ QUARKUS_BRANCH: 'main' ] }
    ),
    QUARKUS_BRANCH(
        optional: true,
        isActiveClosure: { script -> Utils.isQuarkusBranchEnvironmentEnabled(script) },
        getDefaultEnvVarsClosure: { script -> [ QUARKUS_BRANCH: Utils.getQuarkusEnvironmentBranchName(script) ] }
    ),
    KOGITO_BDD(
        optional: true,
        isActiveClosure: { script -> Utils.isRuntimesBDDEnvironmentEnabled(script) },
    )

    boolean optional
    Closure isActiveClosure
    Closure getDefaultEnvVarsClosure

    String toId() {
        return this.name().toLowerCase().replaceAll('_', '-')
    }

    String toName() {
        return Utils.getRepoNameCamelCase(this.toId())
    }

    boolean isOptional() {
        return this.optional
    }

    boolean isActive(def script) {
        return !this.isOptional() || (this.isActiveClosure ? this.isActiveClosure(script) : true)
    }

    Map getDefaultEnvVars(def script) {
        return this.getDefaultEnvVarsClosure ? this.getDefaultEnvVarsClosure(script) : [:]
    }

    static List<Environment> getActiveEnvironments(def script) {
        return Environment.values().findAll { environment -> environment.isActive(script) }
    }

}