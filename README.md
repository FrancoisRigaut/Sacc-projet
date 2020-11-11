# Sacc-projet
 Projet Software Architecture in Cloud Computing

## Our super team
- Boulat Pierre-Antoine
- Masia Sylvain
- Montoya Damien
- Rigaut François
- Richard Peres

## Building this project

Launch the google cloud console then launch the following commands
- Init gcloud `gcloud init`
- Create the project `gcloud projects create sacc-onfine`
  - If you were on another project type `gcloud config set project sacc-onfine`
- You can check that the project is selected by typing `gcloud config get-value project`
- Create the app in the project `gcloud app create` then choose `europe-west`
- Install java component `gcloud components install app-engine-java`
- Create maven base app `mvn archetype:generate -Dappengine-version=1.9.82 -Dapplication-id=sacc-onfine -Dfilter=com.google.appengine.archetypes:appengine-standard-archetype`
  - Type 1 then choose version 1.0.2
  - groupId : `polytech.sacc`
  - artifactId: `polytech.sacc.onfine`
  - package: `polytech.sacc.onfine`

Do not forget to `mvn clean install` the first time

Pour déployer et pomper les crédits de Damien : `mvn package appengine:deploy -Dapp.deploy.projectId=sacc-onfine`

## Creating task and queue

To allow using task you need to set the env credentials var. Type `set GOOGLE_APPLICATION_CREDENTIALS=C:\dev\courses\Sacc-projet\sacc-onfine-dc042eb66eba.json`
  - If you are running server with intelliJ, you need to set the configuration env var in Edit Configurations >  Startup Connection

To create a task follow the steps
- `gcloud tasks queues create TASK_NAME
- Wait few minutes and then `gcloud tasks queues describe TASK_NAME` to check if the task is up

## How to use

You can launch the project in local by running `mvn appengine:run` or by using the IntelliJ plugin

To deploy online `mvn package appengine:deploy -Dapp.deploy.projectId=sacc-onfine`
