import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import GyroConnectionViewB from './gyroConnectionB';
import AccelConnectionViewB from './accelConnectionB';
import LevelBattery from './infoSensor/batteryLevel';
import InfoDevice from './infoSensor/info';

interface HomeProps {
  device: string;
}
export const HomeConnectionBleB = ({ device }: HomeProps) => {
  return (
    <View style={styles.container}>
      <Text style={styles.textStyle}>Device: {device}</Text>
      <LevelBattery device={device} />
      <InfoDevice device={device} />
      <AccelConnectionViewB device={device} />
      <GyroConnectionViewB device={device} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: 'white',
    flex: 1,
    flexDirection: 'column',
    padding: 10,
    borderRadius: 10,
  },
  textStyle: {
    textAlign: 'center',
    fontSize: 20,
    fontWeight: 'bold',
    backgroundColor: '#F7C4B9',
  },
  TempSection: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
});
