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

Before installing deploy Helm chart you need to install [configmap Helm chart](../configmap). Than you need to set variables in **values.yaml** file using any code editor. Some of the values are prefilled, but you need to specify some values as well. You can find more information about them below.

### Common variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**logLevel** | logging level | string | INFO | yes
**sisData**  | path to Apache SIS library | string | "/crs-converter/apachesis_setup/SIS_DATA" | yes
**storageHost** | host to Storage service | string | "http://storage" | yes
**entitlementsHost** | host to Entitlements service | string | "http://entitlements" | yes

### Config variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**configmap** | configmap to be used | string | crs-conversion-config | yes
**appName** | name of the app | string | crs-conversion | yes

### Install the helm chart

Run this command from within this directory:

```console
helm install gcp-crs-conversion-deploy .
```

## Uninstalling the Chart

To uninstall the helm release:

```console
helm uninstall gcp-crs-conversion-deploy
```

[Move-to-Top](#deploy-helm-chart)
