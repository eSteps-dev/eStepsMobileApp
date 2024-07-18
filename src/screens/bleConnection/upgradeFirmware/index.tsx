import React, { useEffect } from 'react';
import { NativeModules, View, Text, StyleSheet } from 'react-native';

interface CalibrationProps {
  device: string;
}
const UpgradeFirmware = ({ device }: CalibrationProps) => {
  const { upgradeFirmwareVersion } = NativeModules;

  useEffect(() => {
    upgradeFirmwareVersion.bindUpgradeFirmware(device);

    return () => {
      upgradeFirmwareVersion.unbindUpgradeFirmware();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Device: {device}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  button: {
    alignItems: 'center',
    backgroundColor: '#eee',
    flex: 1,
    justifyContent: 'center',
    padding: 10,
  },
  buttonContainer: {
    alignItems: 'stretch',
    flexDirection: 'row',
    marginTop: 15,
  },
  container: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  text: {
    textAlign: 'center',
  },
});

export default UpgradeFirmware;
