
import { PubSub } from '@aws-amplify/pubsub';

// Apply plugin with configuration
export const pubsub = new PubSub({
  region: 'eu-north-1',
  endpoint:
    'wss://a2v2z5njl5hwza-ats.iot.eu-north-1.amazonaws.com/mqtt'
});