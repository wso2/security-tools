FROM ubuntu:16.04

RUN apt-get update

RUN apt-get install -y wget
 
RUN apt-get install -y git

RUN apt-get install unzip

# download Maven
RUN wget --no-verbose -O /tmp/apache-maven-3.3.9.tar.gz http://archive.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz

#install Maven
RUN tar xzf /tmp/apache-maven-3.3.9.tar.gz -C /opt/
RUN ln -s /opt/apache-maven-3.3.9 /opt/maven
RUN ln -s /opt/maven/bin/mvn /usr/local/bin
RUN rm -f /tmp/apache-maven-3.3.9.tar.gz
ENV MAVEN_HOME /opt/maven 
ENV M2_HOME /opt/maven

# remove download archive files 
RUN apt-get clean

# set shell variables for java installation 
ENV java_version 1.8.0_144 
ENV filename jdk-8u144-linux-x64.tar.gz 
ENV download_link http://download.oracle.com/otn-pub/java/jdk/8u144-b01/090f390dda5b47b9b721c7dfaa008135/$filename

# download java, accepting the license agreement 
RUN wget -c --header "Cookie: oraclelicense=accept-securebackup-cookie" -O /tmp/$filename $download_link

# unpack java 
RUN mkdir /opt/java-oracle && tar -zxf /tmp/$filename -C /opt/java-oracle/
ENV JAVA_HOME /opt/java-oracle/jdk$java_version 
ENV PATH $JAVA_HOME/bin:$PATH 

# configure symbolic links for the java and javac executables 
RUN update-alternatives --install /usr/bin/java java $JAVA_HOME/bin/java 20000 && update-alternatives --install /usr/bin/javac javac $JAVA_HOME/bin/javac 20000

#set shell variables to download dynamic_scanner
ENV scanner_git_path https://github.com/deshanigtk/MicroServiceApp-ProductManager-JAR.git
ENV git_clone_dir MicroServiceApp-ProductManager-JAR
ENV jar_file product-manager-1.0-SNAPSHOT.jar
ENV port 8081

RUN git clone $scanner_git_path /opt/$git_clone_dir

WORKDIR /opt/$git_clone_dir

CMD ["sh", "-c", "java -jar $jar_file --server.port=$port"]
