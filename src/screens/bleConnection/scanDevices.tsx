import React, { useEffect, useState } from 'react';
import { View, Text, PermissionsAndroid, StyleSheet, ScrollView } from 'react-native';
import { BleManager } from 'react-native-ble-plx';
import { HomeConnectionBleA } from './connectionBleA';
import { HomeConnectionBleB } from './connectionBleB';

const requestLocationPermission = async () => {
  try {
    const granted = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION, {
      title: 'Location Permission',
      message: 'This app requires access to your location to scan for BLE devices.',
      buttonPositive: 'OK',
    });
    if (granted === PermissionsAndroid.RESULTS.GRANTED) {
      console.log('Location permission granted');
    } else {
      console.log('Location permission denied');
    }
  } catch (error) {
    console.error('Error requesting location permission:', error);
  }
};

export function ScanDevices(): React.JSX.Element {
  const [accesGranted, setAccesGranted] = useState<boolean>(false);
  const [devices, setDevices] = useState<any>([]);

  useEffect(() => {
    requestLocationPermission().then(() => {
      setAccesGranted(true);
    });
  }, []);

  useEffect(() => {
    if (accesGranted) {
      const manager = new BleManager();
      const subscription = manager.onStateChange((state) => {
        if (state === 'PoweredOn') {
          scanAndConnect(manager);
          subscription.remove();
        }
      }, true);
      return () => {
        subscription.remove();
        manager.destroy(); // Clean up the BleManager instance
      };
    }
  }, [accesGranted]);

  const scanAndConnect = (manager: BleManager) => {
    const devicesList = devices;
    manager.startDeviceScan(null, null, (error, device) => {
      if (error) {
        return;
      }
      const existDev = devicesList.find((dev: any) => dev === device?.id);
      if (device?.name === 'MetaWear' && !existDev) {
        devicesList.push(device.id);
        setDevices([...devicesList]); // Update the devices state
      }
    });
    if (devicesList.length === 2) {
      manager.stopDeviceScan();
      return;
    }
  };
  if (devices.length !== 2) {
    return (
      <View style={styles.loading}>
        <Text style={styles.loadingText}>Loading ..........</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      {devices.length === 2 && (
        <>
          <View key={devices?.[0]} style={styles.deviceContainer}>
            <HomeConnectionBleA device={devices?.[0]} />
          </View>
          <View key={devices?.[1]} style={styles.deviceContainer}>
            <HomeConnectionBleB device={devices?.[1]} />
          </View>
        </>
      )}
    </ScrollView>
  );
}
const styles = StyleSheet.create({
  container: {
    flex: 1,
  },

  deviceContainer: {
    height: 400,
    padding: 10,
  },
  loading: {
    flex: 1,
    textAlign: 'center',
    alignItems: 'center',
    justifyContent: 'center',
  },
  loadingText: {
    fontSize: 20,
  },
});
