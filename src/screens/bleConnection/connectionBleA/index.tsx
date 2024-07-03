import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import TempViewA from './tempConnection';
import AccelConnectionViewA from './accelConnection';
import GyroConnectionViewA from './gyroConnection';
import CalibrateSensor from '../calibrateSensor';

interface HomeProps {
  device: string;
  client: string;
}
export const HomeConnectionBleA = ({ device, client }: HomeProps) => {
  return (
    <View style={styles.container}>
      <Text style={styles.textStyle}>Device: {device}</Text>
      {/* <CalibrateSensor device={device} /> */}
      <TempViewA device={device} client={client} />

      <AccelConnectionViewA device={device} client={client} />
      <GyroConnectionViewA device={device} client={client} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: 'white',
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'space-between',
    padding: 10,
  },
  textStyle: {
    textAlign: 'center',
    fontSize: 20,
    fontWeight: 'bold',
    backgroundColor: '#F7C4B9',
  },
});
