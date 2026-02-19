# Software-Team-16
Joss Jongewaard -- JossJongewaard

Joseph Peraza -- JoeP05-05

Kaija Frierson -- kaijafrierson

Taija Frierson -- Taija797

## Step 1
Update the virtual machine

Use the following line on the command line:
```
sudo apt update
```

## Step 2 
Install Java on to the virtual machine

```
sudo apt install default-jdk -y
```
Make sure that java is on the current version available, which can be seen by:
```
java -version
```

## Step 3
Install Git, and PostgreSQL, and wget:
```
sudo apt install git postgresql postgresql-contrib netcat-openbsd wget -y
```

## Step 4: Install/Update PostgreSQL
```
sudo apt install postgresql postgresql-contrib -y
```

## To Update Database:
Set up Database: 
```
sudo -u postgres psql 
```
Create the database

Connect to new database:  
```
\c photon 
```

If "photon" not (in database) there: 
```
CREATE DATABASE photon 
```

Create player table: 
```
CREATE TABLE players (id SERIAL PRIMARY KEY, name VARCHAR(100), team VARCHAR(10), equipment_id INTEGER UNIQUE);
```

Check to see if table created:
```
\d players 
```
Command to exit the PostgreSQL:
```
\q 
```

USER TRUST AUTHENTICATION: 

Admin creation 
```
sudo -u postgres psql 
```
                                 
Create the user: 
```
CREATE USER student; 
```                                    
Make user supervisor: 
```
ALTER USER student WITH SUPERUSER; 
```                                    
Grant permission photon database: 
```
GRANT ALL PRIVILEGES ON DATABASE photon TO student; 
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO student;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO student;
```
Exit PostgreSQL: 
```
\q 
```
TRUST AUTHENTICATION:
```
sudo -u postgres psql -c "SHOW hba_file;"
sudo nano /etc/postgresql/13/main/pg_hba.conf
```
Add this line before IPv4 connections:
```
host    photon          student         127.0.0.1/32    trust
```

Exit nano file and RESTART PostgreSQL:
```
sudo systemctl restart postgresql
```

## Step 5: Cloning the repositiory on the Virtual machine and getting into the right folder
```
git clone https://github.com/JoeP05-05/Software-Team-16.git
cd Software-Team-16
cd Project
cd code
```

## Step 6: Run the Install Script
```
chmod +x install.sh
./install.sh
```

## Step 7: Run the test file
```
javac -cp .:postgresql-42.7.4.jar test.java
java -cp .:postgresql-42.7.4.jar test
```

## Step 8: Run Main
```
javac -cp .:postgresql-42.7.4.jar *.java
java -cp .:postgresql-42.7.4.jar Main
```



