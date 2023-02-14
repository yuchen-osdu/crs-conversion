<!--- Deploy -->

# Deploy helm chart

## Introduction

This chart bootstraps a deployment on a [Kubernetes](https://kubernetes.io) cluster using [Helm](https://helm.sh) package manager.

## Prerequisites

The code was tested on **Kubernetes cluster** (v1.21.11) with **Istio** (1.12.6)
> It is possible to use other versions, but it hasn't been tested

### Operation system

The code works in Debian-based Linux (Debian 10 and Ubuntu 20.04) and Windows WSL 2. Also, it works but is not guaranteed in Google Cloud Shell. All other operating systems, including macOS, are not verified and supported.

### Packages

Packages are only needed for installation from a local computer.

- **HELM** (version: v3.7.1 or higher) [helm](https://helm.sh/docs/intro/install/)
- **Kubectl** (version: v1.21.0 or higher) [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)

## Installation

First you need to set variables in **values.yaml** file using any code editor. Some of the values are prefilled, but you need to specify some values as well. You can find more information about them below.

### Configmap variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.logLevel** | logging level | string | INFO | yes
**data.sisData**  | path to Apache SIS library | string | "/crs-converter/apachesis_setup/SIS_DATA" | yes
**data.storageHost** | Storage service host address | string | "http://storage" | yes
**data.entitlementsHost** | Entitlements service host address | string | "http://entitlements" | yes

### Deployment variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.image** | path to the image in a registry | string | - | yes
**data.requestsCpu** | amount of requests CPU | string | `20` | yes
**data.requestsMemory** | amount of requests memory| string | `350Mi` | yes
**data.limitsCpu** | CPU limit | string | `1` | yes
**data.limitsMemory** | memory limit | string | `1G` | yes
**data.serviceAccountName** | name of kubernetes service account | string | `crs-conversion` | yes
**data.imagePullPolicy** | when to pull the image | string | `IfNotPresent` | yes

### Config variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**conf.configmap** | configmap to be used | string | crs-conversion-config | yes
**conf.appName** | name of the app | string | crs-conversion | yes
**conf.onPremEnabled** | whether on-prem is enabled | boolean | false | yes
**conf.domain** | your domain | string | - | yes

### ISTIO variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**istio.proxyCPU** | CPU request for Envoy sidecars | string | 10m | yes
**istio.proxyCPULimit** | CPU limit for Envoy sidecars | string | 500m | yes
**istio.proxyMemory** | memory request for Envoy sidecars | string | 100Mi | yes
**istio.proxyMemoryLimit** | memory limit for Envoy sidecars | string | 512Mi | yes

### Install the helm chart

Run this command from within this directory:

```console
helm install gc-crs-conversion-deploy .
```

## Uninstalling the Chart

To uninstall the helm release:

```console
helm uninstall gc-crs-conversion-deploy
```

[Move-to-Top](#deploy-helm-chart)
