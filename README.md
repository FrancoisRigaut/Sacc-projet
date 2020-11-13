# Sacc-projet
 Projet Software Architecture in Cloud Computing

## Our super team
- Boulat Pierre-Antoine
- Masia Sylvain
- Montoya Damien
- Rigaut François
- Peres Richard

# Using project

//TODO describe to prof ce qu'il faut faire

# Create and manage Google Cloud Project

Attention to the teacher : the following steps explain the way we managed our Google Cloud project, so you do not have to read this part.

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

To allow using task you need to set the env credentials var. Type `set GOOGLE_APPLICATION_CREDENTIALS=PATH_TO\sacc-onfine-dc042eb66eba.json`
  - If you are running server with intelliJ, you need to set the configuration env var in Edit Configurations >  Startup Connection

To create a task follow the steps
- `gcloud tasks queues create TASK_NAME`
- Wait few minutes and then `gcloud tasks queues describe TASK_NAME` to check if the task is up

## Creating MySQL database

First get to the Google Cloud Console. Then get to the Sql > Instances page.  
Click on "Create instance". Choose for example PostgreSQL.  
Choose your database ID `pg-instance-sacc` and the password for postgres user can be `superpassword`.  
Also download the postgresql drivers to connect to the database from your computer [HERE](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads).  
Once everything has been created, you can connect to the database by typing the command `gcloud sql connect pg-instance-sacc --user=postgres`.  
Then run `\connect sacc_onfine` to connect to the database.  
Note that the file [POSTGRESQL_script.sql](./POSTGRESQL_script.sql) contains the structure of our database.

To be able to see logs in IntelliJ from Google Cloud, get to Edit configurations > VM Options > `-Dlog4j.configurationFile=PATH_TO\log4j2.xml` 

## How to use

You can launch the project in local by running `mvn appengine:run` or by using the IntelliJ plugin

To deploy online `mvn package appengine:deploy -Dapp.deploy.projectId=sacc-onfine`
