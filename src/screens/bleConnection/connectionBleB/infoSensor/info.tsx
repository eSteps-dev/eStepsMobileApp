import React, { useEffect, useState } from 'react';
import { NativeModules, View, Text, StyleSheet, NativeEventEmitter } from 'react-native';
import { pubsub } from '../../../../pubSub';

interface infoProps {
  device: string;
}
const InfoDevice = ({ device }: infoProps) => {
  const { DeviceInfoB } = NativeModules;
  const eventEmitter = new NativeEventEmitter(DeviceInfoB);

  const [data, setData] = useState<any>({
    serialNumber: '',
    firmwareRevision: '',
    hardwareRevision: '',
    manufacturer: '',
    modelNumber: '',
  });

  useEffect(() => {
    const subscription = eventEmitter.addListener('DeviceInfoB', (DeviceInfoB: any) => {
      const { serialNumber, firmwareRevision, hardwareRevision, manufacturer, modelNumber } = DeviceInfoB;
      const timestamp = new Date().getTime();

      const message = { serialNumber, firmwareRevision, hardwareRevision, manufacturer, modelNumber, timestamp };
      setData(message);

      if (DeviceInfoB) {
        const topics = `esteps/info/${serialNumber}`;
        const sendMqttMessage = async () => {
          await pubsub.publish({
            topics,
            message,
          });
        };
        sendMqttMessage();
      }
    });

    return () => {
      subscription.remove();
    };
  }, []);
  useEffect(() => {
    DeviceInfoB.bindInfoDevice(device);
    return () => {
      DeviceInfoB.unbindInfoDevice();
    };
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.item}>
        <Text style={styles.label}>Serial number:</Text>
        <Text style={styles.value}>{data.serialNumber}</Text>
      </View>

      <View style={styles.item}>
        <Text style={styles.label}>Firmware revision: </Text>
        <Text style={styles.value}>{data.firmwareRevision}</Text>
      </View>

      <View style={styles.item}>
        <Text style={styles.label}>Hardware revision: </Text>
        <Text style={styles.value}>{data.hardwareRevision}</Text>
      </View>

      <View style={styles.item}>
        <Text style={styles.label}>Manufacturer: </Text>
        <Text style={styles.value}>{data.manufacturer}</Text>
      </View>

      <View style={styles.item}>
        <Text style={styles.label}>Model number:</Text>
        <Text style={styles.value}>{data.modelNumber}</Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  item: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  label: {
    fontWeight: 'bold',
  },
  value: {
    flex: 1,
    textAlign: 'right',
  },
});

export default InfoDevice;
