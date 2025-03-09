# Rank and File

# Setup MySql database on localhost
database name should be 'rankandfile'
Set your username and password to whatever you like, reference them in the env variables as shown below
Download the following links for your machine here: https://dev.mysql.com/downloads/
- MySQL Community Server
- MySQL Installer for Windows (for windows) or MySQL Shell (for MacOS)

# Install latest version of liquibase on your machine
Liquibase download: https://docs.liquibase.com/start/install/home.html
Additional Documentation: https://docs.liquibase.com/start/home.html
Use xml file system with .sql files to create database tables

# Congress.gov API
- Signup for a congress.gov api key here: https://api.congress.gov/sign-up/
- It limits requests to 1,000/hr
- read more: https://github.com/LibraryOfCongress/api.congress.gov/
- and the api page here: https://api.congress.gov/#/

# The following env variables are needed
- db_user
- db_pass
- api_key

# Please follow the format below when creating branches:
- Branch name should be: RAF_### - where ### is an incrementing number of the previous branch
- Be as detailed as possible in commit messages and don't delete branch after merging
- Request code review/merge request when raising against 'master' branch

---------------------------------------------------------------------------------------------
# API Documentation
- The RAF API is broken into internal and external endpoints
- external currently go to the api.congress.gov endpoints to load data into our database
- internal endpoints load data to the raf-frontend module

# External Endpoints:
Person (Politician):
- PUT - /api/member/all
- PUT - /api/member/congress/{congressId}
- PUT - /api/member/{bioguideId}
- PUT - /api/member/{bioguideId}/sponsored-legislation
- PUT - /api/member/{bioguideId}/cosponsored-legislation

Bill:
- PUT - /api/bill/{congressNo}
- PUT - /api/bill/{congressNo}/{billType}/{billNumber}
- PUT - /api/bill/{congressNo}/{billType}/{billNumber}/actions
- PUT - /api/bill/{congressNo}/{billType}/{billNumber}/committees
- PUT - /api/bill/{congressNo}/{billType}/{billNumber}/summary
- PUT - /api/bill/{congressNo}/{billType}/{billNumber}/text
- PUT - /api/bill/{congressNo}/{billType}/{billNumber}/subjects
- PUT - /api/bill/{congressNo}/{billType}/{billNumber}/relatedBills
- PUT - /api/bill/{congressNo}/{billType}/{billNumber}/cosponsors

Committee:
- PUT - /api/committee/all

Congress:
- PUT - /api/congress/{congressNo}

# Internal Endpoints:
Person (Politician):
- GET - /api/internal/{searchTerm}
- GET - /api/internal/politician/{personId}
- GET - /api/internal/politician/{personId}/sponsored
- GET - /api/internal/politician/{personId}/cosponsored

Bill:
- GET - /api/internal/bill/{congressNo}/{billType}/{billNo}