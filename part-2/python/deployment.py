#
# This is modified version of code provided by Google at:
# https://github.com/GoogleCloudPlatform/deploymentmanager-samples
#

"""Create configuration to deploy Kubernetes resources."""


def GenerateConfig(context):
  """Generate YAML resource configuration."""

  cluster_types_root = ''.join([
      context.env['project'],
      '/',
      context.properties['clusterType']
      ])
  cluster_types = {
      'Service': ''.join([
          cluster_types_root,
          ':',
          '/api/v1/namespaces/{namespace}/services'
          ]),
      'ConfigMap': ''.join([
                cluster_types_root,
                ':',
                '/api/v1/namespaces/{namespace}/configmaps'
                ]),
      'Deployment': ''.join([
          cluster_types_root,
          '-apps',
          ':',
          '/apis/apps/v1beta1/namespaces/{namespace}/deployments'
          ])
  }

  name_prefix = context.env['deployment'] + '-' + context.env['name']
  port = context.properties['port']

  resources = [{
      'name': name_prefix + '-service',
      'type': cluster_types['Service'],
      'properties': {
          'apiVersion': 'v1',
          'kind': 'Service',
          'namespace': 'default',
          'metadata': {
              'name': name_prefix + '-service',
              'labels': {
                  'id': 'deployment-manager'
              }
          },
          'spec': {
              'type': 'NodePort',
              'ports': [{
                  'port': port,
                  'targetPort': port,
                  'protocol': 'TCP'
              }],
              'selector': {
                  'app': name_prefix
              }
          }
      }
  },{
      'name': name_prefix + '-deployment',
      'type': cluster_types['ConfigMap'],
      'properties': {
        'apiVersion': 'v1',
        'kind': 'ConfigMap',
        'namespace': 'default',
        'metadata': {
            'name': 'dm-configmap'
        },
        'data': {
            'SOME_VARIABLE': 'custom-value-deployed'
        }

  }, {
      'name': name_prefix + '-deployment',
      'type': cluster_types['Deployment'],
      'properties': {
          'apiVersion': 'apps/v1beta1',
          'kind': 'Deployment',
          'namespace': 'default',
          'metadata': {
              'name': name_prefix + '-deployment'
          },
          'spec': {
              'replicas': 1,
              'template': {
                  'metadata': {
                      'labels': {
                          'name': name_prefix + '-deployment',
                          'app': name_prefix
                      }
                  },
                  'spec': {
                      'containers': [{
                          'name': 'container',
                          'image': context.properties['image'],
                          'ports': [{
                              'containerPort': port
                          }]
                      }]
                  }
              }
          }
      }
  }]

  return {'resources': resources}
