import React, { useState, useEffect } from 'react';
import { NativeEventEmitter, View, Text, StyleSheet, NativeModules } from 'react-native';
import { pubsub } from '../../../../pubSub';
import TempViewA from '../tempConnection';

interface BatteryProps {
  device: string;
}
const LevelBattery = ({ device }: BatteryProps) => {
  const { BatteryLevelA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(BatteryLevelA);

  const [data, setData] = useState<any>({ level: '', voltage: '' });

  useEffect(() => {
    const subscription = eventEmitter.addListener('batteryLevelA', (batteryLevelA: any) => {
      const { level, serialNumber, voltage } = batteryLevelA;
      const timestamp = new Date().getTime();
      setData({ level, voltage });

      if (batteryLevelA) {
        const message = { level, timestamp, voltage };
        const topics = `esteps/battery/${serialNumber}`;
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
    BatteryLevelA.bindLevelBatteryService(device);
    return () => {
      BatteryLevelA.removeListeners('false');
      BatteryLevelA.unbindLevelBatteryService();
    };
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.item}>
        <TempViewA device={device} />
      </View>
      <Text style={[styles.text, styles.item]}>Batt: {data.level} %</Text>
      <Text style={[styles.text, styles.item]}>Vol: {Number(data.voltage).toFixed(2)} V</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#E9DCD9',
    flexDirection: 'row',
    justifyContent: 'center',
  },
  text: {
    textAlign: 'center',
  },
  item: {
    marginHorizontal: 8,
  },
});

export default LevelBattery;
