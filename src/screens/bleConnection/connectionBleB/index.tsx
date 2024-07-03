import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import TempViewB from './tempConnectionB';
import AccelConnectionViewB from './accelConnectionB';
import GyroConnectionViewB from './gyroConnectionB';

interface HomeProps {
  device: string;
  client: string;
}
export const HomeConnectionBleB = ({ device, client }: HomeProps) => {
  return (
    <View style={styles.container}>
      <Text style={styles.textStyle}>Device: {device}</Text>
      <TempViewB device={device} client={client} />
      <AccelConnectionViewB device={device} client={client} />
      <GyroConnectionViewB device={device} client={client} />
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
