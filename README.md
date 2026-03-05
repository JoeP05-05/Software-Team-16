# Software-Team-16
Joss Jongewaard -- JossJongewaard

Joseph Peraza -- JoeP05-05

Kaija Frierson -- kaijafrierson

Taija Frierson -- Taija797

## Step 1
Install Git:
```
sudo apt install git -y
```

## Step 2: Cloning the repositiory on the Virtual machine and getting into the right folder
```
git clone https://github.com/JoeP05-05/Software-Team-16.git
cd Software-Team-16
cd Project
cd code
```

## Step 3: Run the Install Script
```
chmod +x install.sh
./install.sh
```

## Step 4: Run the test file
```
javac -cp .:postgresql-42.7.4.jar test.java
java -cp .:postgresql-42.7.4.jar test
```

## Step 5: Run Main
```
javac -cp .:postgresql-42.7.4.jar *.java
java -cp .:postgresql-42.7.4.jar Main
```



