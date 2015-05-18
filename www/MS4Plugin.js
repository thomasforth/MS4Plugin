var MS4Plugin = {
	testMessage: function (message, successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback, 'MS4Plugin', 'testMessage', [{
			"message": message
        }]);
	},
	open: function (successCallback, errorCallback, bundleName) {
		if (typeof bundleName == null) {
			cordova.exec(successCallback, errorCallback, 'MS4Plugin', 'openScanner', []);
		} else {
			cordova.exec(successCallback, errorCallback, 'MS4Plugin', 'openScanner', [{
				"bundleName": bundleName
        	}]);
		}
	},
	sync: function (successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback, 'MS4Plugin', 'syncScanner', []);
	},
	recognisePhotoBase64: function (successCallback, errorCallback, photoBase64String) {
		cordova.exec(successCallback, errorCallback, 'MS4Plugin', 'recognisePhotoString', [{
			"photoString": photoBase64String
        }]);
	},
	scan: function (successCallback, errorCallback, scanOptions, scanFlags) {
		// grab parameters from the scanOptions object
		// Scan formats
		var scanFormats = {
			ean8: 1 << 0,
			/* EAN8 linear barcode */
			ean13: 1 << 1,
			/* EAN13 linear barcode */
			qrcode: 1 << 2,
			/* QR Code 2D barcode */
			dmtx: 1 << 3,
			/* Datamatrix 2D barcode */
			image: 1 << 31 /* Image match */
		}

		var formats = 0;
		// Compile the selected scanning formats into a single Hex code the Moodstocks Native Plugin understands
		for (strFormat in scanFormats) {
			if (scanOptions[strFormat]) {
				formats |= scanFormats[strFormat]; // this it the Bitwise OR Assignment Operator 
			}
		}

		// grab parameters from the scanFlags object. Use defaults if not provided
		if (typeof (scanFlags['useDeviceOrientation']) !== null) {
			deviceOrientation = scanFlags['useDeviceOrientation']
		} else {
			deviceOrientation = false;
		}
		if (typeof (scanFlags['noPartialMatching']) !== null) {
			noPartial = scanFlags['noPartialMatching'];
		} else {
			noPartial = false;
		}
		if (typeof (scanFlags['smallTargetSupport']) !== null) {
			smallTarget = scanFlags['smallTargetSupport'];

		} else {
			smallTarget = false;
		}
		if (typeof (scanFlags['returnQueryFrame']) !== null) {
			returnQueryFrame = scanFlags['queryFrame'];
		} else {
			returnQueryFrame = false;
		}

		cordova.exec(successCallback, errorCallback, 'MS4Plugin', 'startScan', [{
			"scanType": scanOptions['scanType'],
			"scanFormats": formats,
			"useDeviceOrientation": deviceOrientation,
			"noPartialMatching": noPartial,
			"smallTargetSupport": smallTarget,
			"returnQueryFrame": returnQueryFrame,
        }]);
	},
	dismiss: function (successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback, 'MS4Plugin', 'stopScan', []);
	},
	pause: function (successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback, 'MS4Plugin', 'pauseScan', []);
	},
	resume: function (successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback, 'MS4Plugin', 'resumeScan', []);
	},
	tapToScan: function (successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback, 'MS4Plugin', 'tapToScan', []);
	}
}
module.exports = MS4Plugin;