{{/* vim: set filetype=mustache: */}}

{{/*
Expand the name of the chart.
We truncate to 20 characters because this is used to set the node identifier in WildFly which is limited to
23 characters. This allows for a replica suffix for up to 99 replicas.
*/}}
{{- define "eric-sc-hcagent.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 20 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create version as used by the chart label.
*/}}
{{- define "eric-sc-hcagent.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{/*
Define seccompprofile for the entire hc-agent pod
*/}}
{{- define "eric-sc-hcagent.podSeccompProfile" -}}
{{- if and .Values.seccompProfile .Values.seccompProfile.type }}
seccompProfile:
  type: {{ .Values.seccompProfile.type }}
{{- if eq .Values.seccompProfile.type "Localhost" }}
  localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
{{- end }}
{{- end }}
{{- end -}}

{{/*
Define seccompProfile for hc-agent container
*/}}
{{- define "eric-sc-hcagent.hcagent.seccompProfile" -}}
{{- if .Values.seccompProfile -}}
{{- if and .Values.seccompProfile.hcagent .Values.seccompProfile.hcagent.type }}
seccompProfile:
  type: {{ .Values.seccompProfile.hcagent.type }}
{{- if eq .Values.seccompProfile.hcagent.type "Localhost" }}
  localhostProfile: {{ .Values.seccompProfile.hcagent.localhostProfile }}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Generate labels helper function
*/}}
{{- define "eric-sc-hcagent.generate-peer-labels" -}}
{{- $peers := index . "peers" -}}
{{- $peerLabels := dict }}
{{- range $_, $peer := $peers }}
    {{- $_ := set $peerLabels ((list $peer "access") | join "-") "true" -}}
{{- end }}
{{- toYaml $peerLabels }}
{{- end -}}

{{- define "eric-sc-hcagent.pod.labels" -}}
{{- $podLabelsDict := dict }}
{{- $_ := set $podLabelsDict "app" (include "eric-sc-hcagent.name" . | toString) }}
{{- $_ := set $podLabelsDict "release" .Release.Name }}
{{- $peerLabels := include "eric-sc-hcagent.peer.labels" . | fromYaml -}}
{{- $baseLabels := include "eric-sc-hcagent.labels" . | fromYaml -}}
{{- include "eric-sc-hcagent.mergeLabels" (dict "location" .Template.Name "sources" (list $podLabelsDict $peerLabels $baseLabels)) | trim}}
{{- end -}}

{{/*
Define labels for Network Policies
*/}}
{{- define "eric-sc-hcagent.peer.labels" -}}
{{- $peers := list }}
{{- if eq (include "eric-sc-hcagent.logshipper-enabled" . ) "true" }}
    {{- $peers = append $peers .Values.adp.log.transformer.hostname }}
{{- end }}
{{- $peers = append $peers .Values.adp.fh.alarmhandler.hostname }}
{{- template "eric-sc-hcagent.generate-peer-labels" (dict "peers" $peers) }}
{{- end -}}

{{/*
Define TLS, note: returns boolean as string
*/}}
{{- define "eric-sc-hcagent.tls" -}}
{{- $hcatls := "true" -}}
{{- if .Values.global -}}
    {{- if .Values.global.security -}}
        {{- if .Values.global.security.tls -}}
            {{- if hasKey .Values.global.security.tls "enabled" -}}
                {{- $hcatls = .Values.global.security.tls.enabled -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $hcatls -}}
{{- end -}}

{{/*
Create hcagent container image registry url
*/}}
{{- define "eric-sc-hcagent.hcagent.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.hcagent.registry -}}
{{- if (((.Values.imageCredentials).hcagent).registry).url -}}
    {{- $url = .Values.imageCredentials.hcagent.registry.url -}}
{{- else if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-sc-hcagent.pullSecrets" -}}
{{- $pullSecret := "" -}}
{{- if (.Values.imageCredentials).pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
{{- else if (.Values.global).pullSecret -}}
    {{- $pullSecret = .Values.global.pullSecret -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create image pull policy hcagent container
*/}}
{{- define "eric-sc-hcagent.hcagent.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((((.Values.imageCredentials).hcagent).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.imageCredentials.hcagent.registry.imagePullPolicy -}}
{{- else if (((.Values.global).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create nodeSelector
*/}}
{{- define "eric-sc-hcagent.nodeSelector" -}}
{{- $nodeSelector := dict -}}
{{- if .Values.global -}}
    {{- if .Values.global.nodeSelector -}}
        {{- $nodeSelector = .Values.global.nodeSelector -}}
    {{- end -}}
{{- end -}}
{{- if .Values.nodeSelector }}
    {{- range $key, $localValue := .Values.nodeSelector -}}
      {{- if hasKey $nodeSelector $key -}}
          {{- $globalValue := index $nodeSelector $key -}}
          {{- if ne $globalValue $localValue -}}
            {{- printf "nodeSelector \"%s\" is specified in both global (%s: %s) and service level (%s: %s) with differing values which is not allowed." $key $key $globalValue $key $localValue | fail -}}
          {{- end -}}
      {{- end -}}
    {{- end -}}
    {{- $nodeSelector = merge $nodeSelector .Values.nodeSelector -}}
{{- end -}}
{{- if $nodeSelector -}}
    {{- toYaml $nodeSelector | indent 8 | trim -}}
{{- end -}}
{{- end -}}

{{- define "eric-sc-hcagent.helm-annotations" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{regexReplaceAll "(.*)[+-].*" .Chart.Version "${1}" }}
{{- end}}

{{ define "eric-sc-hcagent.config-annotations" }}
{{- if .Values.annotations -}}
{{- range $name, $config := .Values.annotations }}
{{ $name }}: {{ tpl $config $ }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-sc-hcagent.labels" -}}
{{- include "eric-sc-hcagent.de-facto-labels" . -}}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end -}}
{{- end -}}

{{- define "eric-sc-hcagent.hcagent.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.hcagent.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.hcagent -}}
        {{- if .Values.imageCredentials.hcagent.repoPath }}
            {{- $repoPath = .Values.imageCredentials.hcagent.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-sc-hcagent.hcagent.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.hcagent.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sc-hcagent.hcagent.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.hcagent.tag -}}
{{- print $image -}}
{{- end -}}

{{/*
If the timezone isn't set by a global parameter, set it to UTC
*/}}
{{- define "eric-sc-hcagent.timezone" -}}
{{- if .Values.global -}}
    {{- .Values.global.timezone | default "UTC" | quote -}}
{{- else -}}
    "UTC"
{{- end -}}
{{- end -}}

{{/*
Return the log streaming type, default is indirect
*/}}
{{- define "eric-sc-hcagent.streamingMethod" -}}
{{ $streamingMethod := "indirect"}}
{{- if (.Values.log).streamingMethod -}}
    {{- $streamingMethod = .Values.log.streamingMethod -}}
{{- else if ((.Values.global).log).streamingMethod -}}
    {{- $streamingMethod = .Values.global.log.streamingMethod -}}
{{- end -}}
{{- if not (has $streamingMethod (list "indirect" "direct" "dual")) -}}
    {{- fail "Incorrect value for streamingMethod in either global or local-HCAGENT values.yaml. Possible values: indirect, direct or dual" -}}
{{- end -}}
{{- print $streamingMethod -}}
{{- end -}}

{{/*
Define LOGBACK file to be used, note: returns logback xml file
*/}}
{{- define "eric-sc-hcagent.logbackFileName" -}}
{{- $streamingMethod := include "eric-sc-hcagent.streamingMethod" . -}}
{{- $fileName := "logbackInDirect.xml" -}}
{{- if eq "direct" $streamingMethod -}}
    {{- $fileName = "logbackDirect.xml" -}}
{{- else if eq "dual" $streamingMethod -}}
    {{- $fileName = "logback.xml" -}}
{{- end -}}
{{- print $fileName -}}
{{- end -}}

{{/*
Determines logshipper sidecar deployment, true if log streamingMethod is "direct" or "dual"
*/}}
{{- define "eric-sc-hcagent.logshipper-enabled" -}}
{{- $streamingMethod := include "eric-sc-hcagent.streamingMethod" . -}}
{{- $enabled := "false" -}}
{{- if or (eq $streamingMethod "direct") (eq $streamingMethod "dual") -}}
    {{- $enabled = "true" -}}
{{- end -}}
{{- print $enabled -}}
{{- end -}}