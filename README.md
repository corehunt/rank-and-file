# Rank and File

# Setup MySql database on localhost
database name should be 'rankandfile'
you can set your username and password to whatever you like, you'll reference them in your enviornment variables as shown below
Download the following links for your machine here: https://dev.mysql.com/downloads/
- MySQL Community Server
- MySQL Installer for Windows (for windows) or MySQL Shell (for MacOS)

# Install latest version of liquibase on your machine
Liquibase download: https://docs.liquibase.com/start/install/home.html
Additional Documentation: https://docs.liquibase.com/start/home.html
We're using xml file system with .sql files to create database tables

# Congress.gov API
- You can signup for a congress.gov api key here: https://api.congress.gov/sign-up/
- It limits your requests to 1,000/hr
- you can read more on it here: https://github.com/LibraryOfCongress/api.congress.gov/
- and the api page here: https://api.congress.gov/#/

# You will need the following environment variables:
- db_user
- db_pass
- api_key

# Please follow the format below when creating branches:
- Branch name should be: RAF_### - where ### is an incrementing number of the previous branch
- Be as detailed as possible in commit messages and don't delete branch after merging
- Request code review/merge request when raising against 'master' branch