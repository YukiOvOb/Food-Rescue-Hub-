import { useCallback, useEffect, useRef, useState } from 'react';
import jsQR from 'jsqr';
import { useNavigate } from 'react-router-dom';
import axios from '../services/axiosConfig';
import authService from '../services/authService';
import './styles/QRCodeDecoder.css';

const CAMERA_CONSTRAINTS = [
  {
    video: {
      facingMode: { ideal: 'environment' },
      width: { ideal: 1280 },
      height: { ideal: 720 }
    },
    audio: false
  },
  {
    video: { facingMode: 'environment' },
    audio: false
  },
  {
    video: true,
    audio: false
  }
];

const normalizeOrders = (payload) => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.data)) return payload.data;
  if (Array.isArray(payload?.orders)) return payload.orders;
  return [];
};

const QRCodeDecoder = () => {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [cameraActive, setCameraActive] = useState(false);
  const [isScanning, setIsScanning] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [captureResult, setCaptureResult] = useState(null);
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);
  const animationFrameRef = useRef(null);

  const parseResponseData = async (response) => {
    const contentType = response.headers.get('content-type') || '';
    const text = await response.text();

    if (!text) {
      return null;
    }

    if (contentType.includes('application/json')) {
      try {
        return JSON.parse(text);
      } catch (parseError) {
        return { error: text };
      }
    }

    try {
      return JSON.parse(text);
    } catch (parseError) {
      return { error: text };
    }
  };

  const stopCamera = useCallback(() => {
    if (animationFrameRef.current) {
      cancelAnimationFrame(animationFrameRef.current);
      animationFrameRef.current = null;
    }

    const stream = streamRef.current || videoRef.current?.srcObject;
    if (stream) {
      stream.getTracks().forEach((track) => track.stop());
    }

    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }

    streamRef.current = null;
    setCameraActive(false);
    setIsScanning(false);
  }, []);

  useEffect(() => {
    return () => {
      stopCamera();
    };
  }, [stopCamera]);

  const startCamera = async () => {
    // allow HTTPS and localhost
    const isHttps = window.location.protocol === 'https:';
    const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
    const isSecure = window.isSecureContext || isHttps;
    
    if (!isSecure && !isLocalhost) {
      setError('Camera access requires HTTPS or localhost.');
      return;
    }

    if (!navigator.mediaDevices?.getUserMedia) {
      setError('This browser does not support camera access.');
      return;
    }

    setError(null);
    setResult(null);
    setSuccessMessage(null);

    for (const constraints of CAMERA_CONSTRAINTS) {
      try {
        const stream = await navigator.mediaDevices.getUserMedia(constraints);
        streamRef.current = stream;

        if (!videoRef.current) {
          stream.getTracks().forEach((track) => track.stop());
          setError('Video element is not available.');
          return;
        }

        videoRef.current.srcObject = stream;
        videoRef.current.muted = true;

        await videoRef.current.play();

        setCameraActive(true);
        setIsScanning(true);
        return;
      } catch (cameraError) {
        // Try the next camera constraint candidate.
      }
    }

    setError('Cannot access camera. Check browser permissions and retry.');
  };

  useEffect(() => {
    if (!cameraActive || !isScanning) {
      return undefined;
    }

    const scanFrame = () => {
      const videoElement = videoRef.current;
      const canvasElement = canvasRef.current;

      if (!videoElement || !canvasElement) {
        animationFrameRef.current = requestAnimationFrame(scanFrame);
        return;
      }

      if (videoElement.readyState < HTMLMediaElement.HAVE_CURRENT_DATA) {
        animationFrameRef.current = requestAnimationFrame(scanFrame);
        return;
      }

      if (!videoElement.videoWidth || !videoElement.videoHeight) {
        animationFrameRef.current = requestAnimationFrame(scanFrame);
        return;
      }

      const context = canvasElement.getContext('2d', { willReadFrequently: true });
      if (!context) {
        animationFrameRef.current = requestAnimationFrame(scanFrame);
        return;
      }
      canvasElement.width = videoElement.videoWidth;
      canvasElement.height = videoElement.videoHeight;
      context.drawImage(videoElement, 0, 0, canvasElement.width, canvasElement.height);

      const imageData = context.getImageData(0, 0, canvasElement.width, canvasElement.height);
      const decoded = jsQR(imageData.data, imageData.width, imageData.height, {
        inversionAttempts: 'attemptBoth'
      });

      if (decoded?.data) {
        const decodedText = decoded.data.trim();
        setResult(decodedText);
        setError(null);
        setSuccessMessage('QR code detected. You can confirm this order now.');
        stopCamera();
        return;
      }

      animationFrameRef.current = requestAnimationFrame(scanFrame);
    };

    animationFrameRef.current = requestAnimationFrame(scanFrame);
    return () => {
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
        animationFrameRef.current = null;
      }
    };
  }, [cameraActive, isScanning, stopCamera]);

  const decodeWithBackend = async (inputFile) => {
    const formData = new FormData();
    formData.append('file', inputFile);

    try {
      const response = await fetch('/api/pickup-tokens/decode-qrcode', {
        method: 'POST',
        body: formData,
        credentials: 'include'
      });

      const data = await parseResponseData(response);
      
      // 如果后端返回500且错误信息包含null，说明没有QR码
      if (!response.ok) {
        const errorMsg = data?.error || data?.message || '';
        if (errorMsg.includes('null') || errorMsg.includes('NotFoundException')) {
          return ''; // 返回空字符串表示没有QR码
        }
        throw new Error(errorMsg || 'Failed to decode QR code');
      }

      return (data?.content || '').trim();
    } catch (fetchError) {
      // 网络错误或其他错误
      throw fetchError;
    }
  };

  const handleFileChange = (event) => {
    const selectedFile = event.target.files?.[0];
    if (!selectedFile) {
      return;
    }
    setFile(selectedFile);
    setError(null);
    setResult(null);
    setSuccessMessage(null);
  };

  const handleUpload = async () => {
    if (!file) {
      setError('Please select a file first.');
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);
    setSuccessMessage(null);

    try {
      const decodedText = await decodeWithBackend(file);
      setResult(decodedText);
      setFile(null);
      setSuccessMessage('QR image decoded successfully. You can confirm the order now.');
    } catch (uploadError) {
      setError(uploadError.message);
    } finally {
      setLoading(false);
    }
  };

  const capturePhoto = async () => {
    if (!videoRef.current || !canvasRef.current) {
      setError('Camera not ready.');
      return;
    }

    const canvas = canvasRef.current;
    const video = videoRef.current;

    // 设置canvas大小与视频相同
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    const context = canvas.getContext('2d');
    if (!context) {
      setError('Cannot capture photo.');
      return;
    }

    // 绘制当前视频帧到canvas
    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    // 将canvas转换为blob并上传
    canvas.toBlob(async (blob) => {
      if (!blob) {
        setError('Failed to capture photo.');
        return;
      }

      setLoading(true);
      setError(null);
      setCaptureResult(null);

      try {
        const decodedText = await decodeWithBackend(blob);
        
        if (!decodedText || decodedText === '') {
          setCaptureResult('None QRCode');
          setError(null);
        } else {
          setCaptureResult(decodedText);
          setResult(decodedText);
          setSuccessMessage('QR code detected successfully!');
        }
      } catch (captureError) {
        setError(captureError.message);
      } finally {
        setLoading(false);
      }
    }, 'image/jpeg', 0.8);
  };

  const resolveSupplierId = async () => {
    const localUser = authService.getStoredUser();
    if (localUser?.userId || localUser?.supplierId) {
      return localUser.userId ?? localUser.supplierId;
    }

    const currentUser = await authService.getCurrentUser();
    return currentUser?.userId ?? currentUser?.supplierId ?? null;
  };

  const confirmOrderByScannedToken = async () => {
    if (!result) {
      setError('Scan or decode a QR code before confirming.');
      return;
    }

    setConfirming(true);
    setError(null);
    setSuccessMessage(null);

    try {
      const supplierId = await resolveSupplierId();
      if (!supplierId) {
        throw new Error('Cannot resolve supplier account.');
      }

      const storesResponse = await axios.get(`/stores/supplier/${supplierId}`);
      const stores = Array.isArray(storesResponse?.data) ? storesResponse.data : [];

      if (stores.length === 0) {
        throw new Error('No stores found for this supplier.');
      }

      const orderResponses = await Promise.all(
        stores.map((store) => axios.get(`/supplier/orders/${store.storeId}`))
      );

      const allOrders = orderResponses.flatMap((response) => normalizeOrders(response?.data));
      const scannedToken = result.trim();

      const readyOrderMatch = allOrders.find(
        (order) => order?.status === 'READY' && (order?.pickupToken?.qrTokenHash || order?.pickupTokenHash) === scannedToken
      );

      const pendingOrderMatch = allOrders.find(
        (order) => order?.status === 'PENDING' && (order?.pickupToken?.qrTokenHash || order?.pickupTokenHash) === scannedToken
      );

      const pickupReadyOrder = readyOrderMatch || pendingOrderMatch;

      if (!pickupReadyOrder) {
        const paidOrderMatch = allOrders.find(
          (order) => order?.status === 'PAID' && (order?.pickupToken?.qrTokenHash || order?.pickupTokenHash) === scannedToken
        );

        const completedOrderMatch = allOrders.find(
          (order) => order?.status === 'COMPLETED' && (order?.pickupToken?.qrTokenHash || order?.pickupTokenHash) === scannedToken
        );

        if (paidOrderMatch) {
          throw new Error(`Order #${paidOrderMatch.orderId} is paid but not marked READY by supplier yet.`);
        }

        if (completedOrderMatch) {
          throw new Error(`Order #${completedOrderMatch.orderId} is already completed.`);
        }

        throw new Error('No ready order found for this pickup token.');
      }

      await axios.patch(`/orders/${pickupReadyOrder.orderId}/status`, null, {
        params: { status: 'COMPLETED' }
      });

      setSuccessMessage(`Order #${pickupReadyOrder.orderId} marked as completed.`);
    } catch (confirmError) {
      setError(confirmError?.response?.data?.message || confirmError.message || 'Failed to confirm order.');
    } finally {
      setConfirming(false);
    }
  };

  return (
    <div className="qr-decoder-container">
      <div className="qr-decoder-header">
        <h2>QR Code Decoder</h2>
        <button className="dashboard-back-btn" onClick={() => navigate('/dashboard')}>
          Back to Dashboard
        </button>
      </div>

      <div className="camera-section">
        <h3>Real-time Camera Detection</h3>
        {!cameraActive ? (
          <button onClick={startCamera} className="camera-btn">
            Start Camera
          </button>
        ) : null}
        
        {/* Video element always in DOM */}
        <video
          ref={videoRef}
          autoPlay
          playsInline
          className={`camera-video-display ${cameraActive ? 'active' : 'hidden'}`}
          style={{ display: cameraActive ? 'block' : 'none' }}
        />
        
        {cameraActive && (
          <div className="camera-controls">
            <button onClick={capturePhoto} disabled={loading} className="capture-btn">
              {loading ? 'Processing...' : 'Capture & Decode'}
            </button>
            <button onClick={stopCamera} className="stop-camera-btn">
              Stop Camera
            </button>
            <p className="scan-status">Position QR code in frame and click Capture</p>
          </div>
        )}
        
        <canvas ref={canvasRef} style={{ display: 'none' }} />
        
        {captureResult && (
          <div className="capture-result">
            <h4>Decode Result:</h4>
            <div className={`result-content ${captureResult === 'None QRCode' ? 'no-qrcode' : 'has-qrcode'}`}>
              {captureResult}
            </div>
          </div>
        )}
      </div>

      <div className="divider">Or</div>

      <div className="upload-section">
        <h3>Upload QR Code Image</h3>
        <input
          type="file"
          accept="image/*"
          onChange={handleFileChange}
          disabled={loading || confirming}
          className="file-input"
        />

        {file && (
          <p className="file-name">
            Selected: <strong>{file.name}</strong>
          </p>
        )}

        <button
          onClick={handleUpload}
          disabled={!file || loading || confirming}
          className="upload-btn"
        >
          {loading ? 'Decoding...' : 'Decode QR Code'}
        </button>
      </div>

      {error && <div className="error-message">❌ {error}</div>}
      {successMessage && <div className="success-message">✅ {successMessage}</div>}

      {result && (
        <div className="result-message">
          <h3>Decoded Content</h3>
          <div className="result-content">
            <code>{result}</code>
          </div>
          <div className="result-actions">
            <button
              onClick={confirmOrderByScannedToken}
              disabled={confirming}
              className="confirm-btn"
            >
              {confirming ? 'Confirming...' : 'Confirm Order Pickup'}
            </button>
            <button
              onClick={() => navigator.clipboard.writeText(result)}
              className="copy-btn"
            >
              Copy Token
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default QRCodeDecoder;
