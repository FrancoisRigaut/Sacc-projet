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
  - If you were on an other project : `gcloud config set project sacc-onfine`
- Create the app in the project `gcloud app create` then choose `europe-west`
- Install java component `gcloud components install app-engine-java`
- Create maven base app `mvn archetype:generate -Dappengine-version=1.9.82 -Dapplication-id=sacc-onfine -Dfilter=com.google.appengine.archetypes:appengine-standard-archetype`
  - Type 1 then choose version 1.0.2
  - groupId : `polytech.sacc`
  - artifactId: `polytech.sacc.onfine`
  - package: `polytech.sacc.onfine`

Do not forget to `mvn clean install` the first time

## How to use

You can launch the project in local by running `mvn appengine:run` or by using the IntelliJ plugin

To deploy online `mvn package appengine:deploy -Dapp.deploy.projectId=sacc-onfine`
