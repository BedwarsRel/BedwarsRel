mkdir CraftBukkit
cd CraftBukkit
wget https://cdn.getbukkit.org/craftbukkit/craftbukkit-1.8-R0.1-SNAPSHOT-latest.jar
mvn install:install-file -Dfile=craftbukkit-1.8-R0.1-SNAPSHOT-latest.jar -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.8-R0.1-SNAPSHOT -Dpackaging=jar
wget https://cdn.getbukkit.org/craftbukkit/craftbukkit-1.8.3-R0.1-SNAPSHOT-latest.jar
mvn install:install-file -Dfile=craftbukkit-1.8.3-R0.1-SNAPSHOT-latest.jar -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.8.3-R0.1-SNAPSHOT -Dpackaging=jar
wget https://cdn.getbukkit.org/craftbukkit/craftbukkit-1.8.8-R0.1-SNAPSHOT-latest.jar
mvn install:install-file -Dfile=craftbukkit-1.8.8-R0.1-SNAPSHOT-latest.jar -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.8.8-R0.1-SNAPSHOT -Dpackaging=jar
wget https://cdn.getbukkit.org/craftbukkit/craftbukkit-1.9.2-R0.1-SNAPSHOT-latest.jar
mvn install:install-file -Dfile=craftbukkit-1.9.2-R0.1-SNAPSHOT-latest.jar -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.9.2-R0.1-SNAPSHOT -Dpackaging=jar
wget https://cdn.getbukkit.org/craftbukkit/craftbukkit-1.9.4-R0.1-SNAPSHOT-latest.jar
mvn install:install-file -Dfile=craftbukkit-1.9.4-R0.1-SNAPSHOT-latest.jar -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.9.4-R0.1-SNAPSHOT -Dpackaging=jar
wget https://cdn.getbukkit.org/craftbukkit/craftbukkit-1.10.2-R0.1-SNAPSHOT-latest.jar
mvn install:install-file -Dfile=craftbukkit-1.10.2-R0.1-SNAPSHOT-latest.jar -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.10.2-R0.1-SNAPSHOT -Dpackaging=jar
wget https://cdn.getbukkit.org/craftbukkit/craftbukkit-1.11.2.jar
mvn install:install-file -Dfile=craftbukkit-1.11.2.jar -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.11.2-R0.1-SNAPSHOT -Dpackaging=jar
wget https://cdn.getbukkit.org/craftbukkit/craftbukkit-1.12.jar
mvn install:install-file -Dfile=craftbukkit-1.12.jar -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.12-R0.1-SNAPSHOT -Dpackaging=jar