import React, { useState, useEffect } from 'react';
import {
  NativeModules,
  NativeEventEmitter,
  View,
  Text,
  StyleSheet,
  EmitterSubscription,
  TouchableOpacity,
} from 'react-native';
import { pubsub } from '../../../pubSub';

interface AccelProps {
  device: string;
}
const AccelConnectionViewA = ({ device }: AccelProps) => {
  const { AccelConnectionA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(AccelConnectionA);
  const [data, setData] = useState({
    x: 0,
    y: 0,
    z: 0,
  });
  const [subscriptionAccel, setSubscriptionAccel] = useState<boolean>(false);
  useEffect(() => {
    let subscriptionAccelA: EmitterSubscription;
    if (subscriptionAccel) {
      subscriptionAccelA = eventEmitter.addListener('accelDataA', (accelDataA) => {
        const { x, y, z, serialNumber, timestamp } = accelDataA;
        setData((prevData) => ({
          ...prevData,
          x: x?.toFixed(2),
          y: y?.toFixed(2),
          z: z?.toFixed(2),
        }));
        const message = {
          x: x?.toFixed(2),
          y: y?.toFixed(2),
          z: z?.toFixed(2),
          timestamp: new Date(timestamp).getTime(),
        };
        if (accelDataA) {
          const topics = `esteps/accel/${serialNumber}`;
          const sendMqttMessage = async () => {
            await pubsub.publish({
              topics,
              message,
            });
          };
          sendMqttMessage();
        }
      });
    } else subscriptionAccelA?.remove();
  }, [subscriptionAccel]);

  useEffect(() => {
    AccelConnectionA.bindAccelService(device);
    setSubscriptionAccel(true);
    return () => {
      AccelConnectionA.unbindAccelService();
    };
  }, []);
  const subscribe = () => {
    setSubscriptionAccel(true);
    AccelConnectionA.bindAccelService(device);
  };
  const unsubscribe = () => {
    setSubscriptionAccel(false);
    AccelConnectionA.unbindAccelService();
    AccelConnectionA.removeListeners('accelDataA');
  };
  return (
    <View style={styles.container}>
      <View style={styles.labelValuesContainer}>
        <Text style={styles.label}>Accel:</Text>
        <Text style={styles.value}>x: {data.x}</Text>
        <Text style={styles.value}>y: {data.y}</Text>
        <Text style={styles.value}>z: {data.z}</Text>
      </View>

      <TouchableOpacity onPress={subscriptionAccel ? unsubscribe : subscribe}>
        <Text style={subscriptionAccel ? styles.textOn : styles.textOff}>{subscriptionAccel ? 'On' : 'Off'}</Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 10,
    marginTop: 10,
  },
  labelValuesContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  label: {
    fontWeight: 'bold',
  },
  value: {
    marginRight: 8,
    marginLeft: 8,
  },
  textOn: {
    color: 'green',
    fontWeight: 'bold',
    fontSize: 14,
  },
  textOff: {
    color: 'red',
    fontWeight: 'bold',
    fontSize: 14,
  },
});

export default AccelConnectionViewA;
