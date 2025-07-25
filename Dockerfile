####################################################
# START GLOBAL DECLARATION
####################################################
ARG REPO_NAME_DEFAULT=mapping-service
ARG REPO_PORT_DEFAULT=8095
ARG SERVICE_ROOT_DIRECTORY_DEFAULT=/spring/
####################################################
# END GLOBAL DECLARATION
####################################################

####################################################
# Building environment (java & git)
####################################################
FROM eclipse-temurin:24 AS build-env-java
LABEL maintainer=webmaster@datamanager.kit.edu
LABEL stage=build-env

# Install git, python3, pip and bash  as additional requirement
RUN apt-get -y update && \
    apt-get -y upgrade  && \
    apt-get install -y --no-install-recommends git bash python3 python3-pip python3-venv && \
    apt-get clean \
    && rm -rf /var/lib/apt/lists/*

####################################################
# Building service
####################################################
FROM build-env-java AS build-service-mapping-service
LABEL maintainer=webmaster@datamanager.kit.edu
LABEL stage=build-contains-sources

# Fetch arguments from above
ARG REPO_NAME_DEFAULT
ARG SERVICE_ROOT_DIRECTORY_DEFAULT

# Declare environment variables
ENV REPO_NAME=${REPO_NAME_DEFAULT}
ENV SERVICE_DIRECTORY=$SERVICE_ROOT_DIRECTORY_DEFAULT$REPO_NAME

# Create directory for repo
RUN mkdir -p /git/${REPO_NAME}
WORKDIR /git/${REPO_NAME}
COPY . .
RUN cp settings/application-docker.properties settings/application-default.properties
# Build service in given directory
RUN bash ./build.sh $SERVICE_DIRECTORY

####################################################
# Runtime environment 4 metastore2
####################################################
FROM ubuntu:24.04 AS run-service-mapping-service
ENV DEBIAN_FRONTEND=noninteractive
LABEL maintainer=webmaster@datamanager.kit.edu
LABEL stage=run

# Fetch arguments from above
ARG REPO_NAME_DEFAULT
ARG REPO_PORT_DEFAULT
ARG SERVICE_ROOT_DIRECTORY_DEFAULT

# Declare environment variables
ENV REPO_NAME=${REPO_NAME_DEFAULT}
ENV SERVICE_DIRECTORY=${SERVICE_ROOT_DIRECTORY_DEFAULT}${REPO_NAME}
ENV REPO_PORT=${REPO_PORT_DEFAULT}

# Install python3, pip and bash as additional requirements
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        python3 python3-venv python3-pip \
        openjdk-21-jdk \
        curl wget git bash ca-certificates && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN java -version && python3 --version && python3 -m venv /venv

# Copy service from build container
RUN mkdir -p ${SERVICE_DIRECTORY}
WORKDIR ${SERVICE_DIRECTORY}
COPY --from=build-service-mapping-service ${SERVICE_DIRECTORY} ./

# Define repo port 
EXPOSE ${REPO_PORT}
ENTRYPOINT ["bash", "./run.sh"]
