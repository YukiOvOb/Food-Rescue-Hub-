import React, { useState, useRef, useEffect } from 'react';
import './styles/QRCodeDecoder.css';

const QRCodeDecoder = () => {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [cameraActive, setCameraActive] = useState(false);
  const videoRef = useRef(null);
  const canvasRef = useRef(null);

  const parseResponseData = async (response) => {
    const contentType = response.headers.get('content-type') || '';
    const text = await response.text();

    if (!text) {
      return null;
    }

    if (contentType.includes('application/json')) {
      try {
        return JSON.parse(text);
      } catch (err) {
        return { error: text };
      }
    }

    try {
      return JSON.parse(text);
    } catch (err) {
      return { error: text };
    }
  };

  // 启动摄像头
  const startCamera = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ 
        video: { facingMode: 'environment' } 
      });
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        setCameraActive(true);
        setError(null);
      }
    } catch (err) {
      setError('Cannot access camera: ' + err.message);
    }
  };

  // 停止摄像头
  const stopCamera = () => {
    if (videoRef.current && videoRef.current.srcObject) {
      videoRef.current.srcObject.getTracks().forEach(track => track.stop());
      setCameraActive(false);
    }
  };

  // 从视频帧中捕获和解码QRCode
  const captureAndDecode = async () => {
    if (!videoRef.current || !canvasRef.current) return;

    const context = canvasRef.current.getContext('2d');
    canvasRef.current.width = videoRef.current.videoWidth;
    canvasRef.current.height = videoRef.current.videoHeight;
    context.drawImage(videoRef.current, 0, 0);

    canvasRef.current.toBlob(async (blob) => {
      if (!blob) return;

      setLoading(true);
      setError(null);
      setResult(null);

      const formData = new FormData();
      formData.append('file', blob, 'qrcode.png');

      try {
        const response = await fetch('/api/pickup-tokens/decode-qrcode', {
          method: 'POST',
          body: formData,
        });

        const data = await parseResponseData(response);

        if (!response.ok) {
          setError(data?.error || data?.message || 'Failed to decode QR code');
          return;
        }

        setResult(data?.content || '');
        stopCamera();
      } catch (err) {
        setError('Network error: ' + err.message);
      } finally {
        setLoading(false);
      }
    });
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      setFile(selectedFile);
      setError(null);
      setResult(null);
    }
  };

  const handleUpload = async () => {
    if (!file) {
      setError('Please select a file first');
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('/api/pickup-tokens/decode-qrcode', {
        method: 'POST',
        body: formData,
      });

      const data = await parseResponseData(response);

      if (!response.ok) {
        setError(data?.error || data?.message || 'Failed to decode QR code');
        return;
      }

      setResult(data?.content || '');
      setFile(null);
    } catch (err) {
      setError('Network error: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="qr-decoder-container">
      <h2>QR Code Decoder</h2>
      
      {/* 摄像头检测部分 */}
      <div className="camera-section">
        <h3>📷 Real-time Camera Detection</h3>
        {!cameraActive ? (
          <button onClick={startCamera} className="camera-btn">
            Start Camera
          </button>
        ) : (
          <>
            <video 
              ref={videoRef} 
              autoPlay 
              playsInline
              className="camera-video"
            />
            <canvas ref={canvasRef} style={{ display: 'none' }} />
            <div className="camera-controls">
              <button 
                onClick={captureAndDecode} 
                disabled={loading}
                className="capture-btn"
              >
                {loading ? 'Processing...' : 'Capture & Decode'}
              </button>
              <button onClick={stopCamera} className="stop-camera-btn">
                Stop Camera
              </button>
            </div>
          </>
        )}
      </div>

      <div className="divider">Or</div>

      {/* 文件上传部分 */}
      <div className="upload-section">
        <h3>📁 Upload QR Code Image</h3>
        <input
          type="file"
          accept="image/*"
          onChange={handleFileChange}
          disabled={loading}
          className="file-input"
        />

        {file && (
          <p className="file-name">
            Selected: <strong>{file.name}</strong>
          </p>
        )}

        <button
          onClick={handleUpload}
          disabled={!file || loading}
          className="upload-btn"
        >
          {loading ? 'Decoding...' : 'Decode QR Code'}
        </button>
      </div>

      {error && <div className="error-message">❌ {error}</div>}

      {result && (
        <div className="result-message">
          <h3>✅ Decoded Content:</h3>
          <div className="result-content">
            <code>{result}</code>
          </div>
          <button
            onClick={() => {
              navigator.clipboard.writeText(result);
              alert('Copied to clipboard!');
            }}
            className="copy-btn"
          >
            Copy to Clipboard
          </button>
        </div>
      )}
    </div>
  );
};

export default QRCodeDecoder;
