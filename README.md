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

## Step 5: Cloning the repositiory on the Virtual machine and getting into the right folder
```
git clone https://github.com/JoeP05-05/Software-Team-16.git
cd Software-Team-16
```
Then, to get into the correct folder to run the program, do the following:
```
cd Project
cd code
```

## Step 6: Run the Install Script
```
chmod +x install.sh
./install.sh
```

## Step 7: Run the program
```
java -cp .:postgresql-42.7.4.jar Main
```


