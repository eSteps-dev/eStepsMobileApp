import React, { useState, useEffect } from 'react';
import {
  NativeModules,
  NativeEventEmitter,
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  EventSubscription,
} from 'react-native';
import { pubsub } from '../../../pubSub';

interface GyroProps {
  device: string;
}
const GyroConnectionViewA = ({ device }: GyroProps) => {
  const { GyroConnectionA } = NativeModules;
  const eventEmitterGyro = new NativeEventEmitter(GyroConnectionA);

  const [data, setData] = useState({
    x: 0,
    y: 0,
    z: 0,
  });
  const [subscriptionGyro, setSubscriptionGyro] = useState<any>(false);
  useEffect(() => {
    let gyroSubscriptionA: EventSubscription;
    if (subscriptionGyro) {
      gyroSubscriptionA = eventEmitterGyro.addListener('gyroDataA', (gyroDataA) => {
        const { x, y, z, serialNumber, timestamp } = gyroDataA;

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
        if (gyroDataA) {
          const topics = `esteps/gyro/${serialNumber}`;
          const sendMqttMessage = async () => {
            await pubsub.publish({
              topics,
              message,
            });
          };
          sendMqttMessage();
        }
      });
    } else gyroSubscriptionA?.remove();
  }, [subscriptionGyro]);

  useEffect(() => {
    GyroConnectionA.bindGyroService(device);
    setSubscriptionGyro(true);
    // Clean up the GyroConnection and remove listeners when the component unmounts
    return () => {
      GyroConnectionA.unbindGyroService();
    };
  }, []);
  const subscribe = () => {
    setSubscriptionGyro(true);
    GyroConnectionA.bindGyroService(device);
  };
  const unsubscribe = () => {
    setSubscriptionGyro(false);
    GyroConnectionA.unbindGyroService();
    GyroConnectionA.removeListeners('gyroDataA');
  };
  return (
    <View style={styles.container}>
      <View style={styles.labelValuesContainer}>
        <Text style={styles.label}>Gyro:</Text>
        <Text style={styles.value}>x: {data.x}</Text>
        <Text style={styles.value}>y: {data.y}</Text>
        <Text style={styles.value}>z: {data.z}</Text>
      </View>

      <TouchableOpacity onPress={subscriptionGyro ? unsubscribe : subscribe}>
        <Text style={subscriptionGyro ? styles.textOn : styles.textOff}>{subscriptionGyro ? 'On' : 'Off'}</Text>
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

export default GyroConnectionViewA;
