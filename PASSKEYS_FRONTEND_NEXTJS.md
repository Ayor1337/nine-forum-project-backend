# Passkeys 前端集成指南 (Next.js)

## 📋 目录
1. [项目结构](#1-项目结构)
2. [工具函数封装](#2-工具函数封装)
3. [API 服务层](#3-api-服务层)
4. [自定义 Hook](#4-自定义-hook)
5. [登录页面集成](#5-登录页面集成)
6. [注册页面集成](#6-注册页面集成)
7. [用户设置页面](#7-用户设置页面)
8. [错误处理](#8-错误处理)
9. [TypeScript 类型定义](#9-typescript-类型定义)

---

## 1. 项目结构

建议的文件组织结构：

```
your-nextjs-app/
├── src/
│   ├── lib/
│   │   └── passkeys/
│   │       ├── utils.ts          # Base64URL 转换工具
│   │       ├── webauthn.ts       # WebAuthn API 封装
│   │       └── types.ts          # TypeScript 类型定义
│   ├── services/
│   │   └── passkey.service.ts    # API 请求封装
│   ├── hooks/
│   │   └── usePasskey.ts         # React Hook
│   └── app/
│       ├── login/
│       │   └── page.tsx          # 登录页面
│       ├── register/
│       │   └── page.tsx          # 注册页面
│       └── settings/
│           └── passkey/
│               └── page.tsx      # Passkey 管理页面
```

---

## 2. 工具函数封装

### 文件: `src/lib/passkeys/utils.ts`

```typescript
/**
 * Base64URL 编码/解码工具
 */

/**
 * Base64URL 字符串转 Uint8Array
 */
export function base64UrlToUint8Array(base64url: string): Uint8Array {
  // 将 Base64URL 转换为标准 Base64
  const base64 = base64url
    .replace(/-/g, '+')
    .replace(/_/g, '/');

  // 补充 padding
  const padLen = (4 - (base64.length % 4)) % 4;
  const padded = base64 + '='.repeat(padLen);

  // 解码
  const binary = atob(padded);
  const bytes = new Uint8Array(binary.length);

  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }

  return bytes;
}

/**
 * Uint8Array 转 Base64URL 字符串
 */
export function uint8ArrayToBase64Url(uint8Array: Uint8Array): string {
  let binary = '';
  for (let i = 0; i < uint8Array.byteLength; i++) {
    binary += String.fromCharCode(uint8Array[i]);
  }

  return btoa(binary)
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');
}

/**
 * ArrayBuffer 转 Base64URL
 */
export function arrayBufferToBase64Url(buffer: ArrayBuffer): string {
  return uint8ArrayToBase64Url(new Uint8Array(buffer));
}

/**
 * 检测浏览器是否支持 WebAuthn
 */
export function isWebAuthnSupported(): boolean {
  return (
    typeof window !== 'undefined' &&
    window.PublicKeyCredential !== undefined &&
    typeof window.PublicKeyCredential === 'function'
  );
}

/**
 * 检测是否支持条件 UI (自动填充)
 */
export async function isConditionalUISupported(): Promise<boolean> {
  if (!isWebAuthnSupported()) return false;

  try {
    return await PublicKeyCredential.isConditionalMediationAvailable();
  } catch {
    return false;
  }
}
```

---

## 3. API 服务层

### 文件: `src/services/passkey.service.ts`

```typescript
import {
  base64UrlToUint8Array,
  uint8ArrayToBase64Url,
  arrayBufferToBase64Url,
} from '@/lib/passkeys/utils';
import type {
  RegistrationOptions,
  AuthenticationOptions,
  CredentialResponse,
} from '@/lib/passkeys/types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:9966';

/**
 * Passkey API 服务
 */
export class PasskeyService {
  /**
   * 开始注册 - 获取注册选项
   */
  static async startRegistration(
    username: string,
    displayName: string
  ): Promise<RegistrationOptions> {
    const response = await fetch(`${API_BASE_URL}/api/passkey/register/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, displayName }),
    });

    const data = await response.json();

    if (!data.success) {
      throw new Error(data.message || '获取注册选项失败');
    }

    return data.options;
  }

  /**
   * 完成注册 - 发送凭证到后端
   */
  static async finishRegistration(
    username: string,
    credential: CredentialResponse
  ): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/passkey/register/finish`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, credential }),
    });

    const data = await response.json();

    if (!data.success) {
      throw new Error(data.message || '注册失败');
    }
  }

  /**
   * 开始认证 - 获取认证选项
   */
  static async startAuthentication(
    username: string
  ): Promise<AuthenticationOptions> {
    const response = await fetch(`${API_BASE_URL}/api/passkey/authenticate/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username }),
    });

    const data = await response.json();

    if (!data.success) {
      throw new Error(data.message || '获取认证选项失败');
    }

    return data.options;
  }

  /**
   * 完成认证 - 发送签名到后端
   */
  static async finishAuthentication(
    credential: CredentialResponse
  ): Promise<{ token: string; message: string }> {
    const response = await fetch(`${API_BASE_URL}/api/passkey/authenticate/finish`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ credential }),
    });

    const data = await response.json();

    if (!data.success) {
      throw new Error(data.message || '认证失败');
    }

    return {
      token: data.token,
      message: data.message,
    };
  }
}

/**
 * WebAuthn 浏览器 API 封装
 */
export class WebAuthnClient {
  /**
   * 注册 Passkey
   */
  static async register(
    username: string,
    displayName: string
  ): Promise<void> {
    // 1. 获取注册选项
    const options = await PasskeyService.startRegistration(username, displayName);

    // 2. 转换 Base64URL 字段为 ArrayBuffer
    const publicKeyOptions: PublicKeyCredentialCreationOptions = {
      ...options,
      challenge: base64UrlToUint8Array(options.challenge),
      user: {
        ...options.user,
        id: base64UrlToUint8Array(options.user.id),
      },
      excludeCredentials: options.excludeCredentials?.map((cred) => ({
        ...cred,
        id: base64UrlToUint8Array(cred.id),
      })) || [],
    };

    // 3. 调用浏览器 WebAuthn API
    const credential = await navigator.credentials.create({
      publicKey: publicKeyOptions,
    }) as PublicKeyCredential;

    if (!credential) {
      throw new Error('凭证创建失败');
    }

    const response = credential.response as AuthenticatorAttestationResponse;

    // 4. 序列化凭证
    const credentialJson: CredentialResponse = {
      id: credential.id,
      rawId: arrayBufferToBase64Url(credential.rawId),
      response: {
        attestationObject: arrayBufferToBase64Url(response.attestationObject),
        clientDataJSON: arrayBufferToBase64Url(response.clientDataJSON),
      },
      type: credential.type,
    };

    // 5. 发送到后端完成注册
    await PasskeyService.finishRegistration(username, credentialJson);
  }

  /**
   * 使用 Passkey 登录
   */
  static async authenticate(username: string): Promise<string> {
    // 1. 获取认证选项
    const options = await PasskeyService.startAuthentication(username);

    // 2. 转换 Base64URL 字段
    const publicKeyOptions: PublicKeyCredentialRequestOptions = {
      ...options,
      challenge: base64UrlToUint8Array(options.challenge),
      allowCredentials: options.allowCredentials?.map((cred) => ({
        ...cred,
        id: base64UrlToUint8Array(cred.id),
      })) || [],
    };

    // 3. 调用浏览器 WebAuthn API
    const credential = await navigator.credentials.get({
      publicKey: publicKeyOptions,
    }) as PublicKeyCredential;

    if (!credential) {
      throw new Error('认证失败');
    }

    const response = credential.response as AuthenticatorAssertionResponse;

    // 4. 序列化凭证
    const credentialJson: CredentialResponse = {
      id: credential.id,
      rawId: arrayBufferToBase64Url(credential.rawId),
      response: {
        authenticatorData: arrayBufferToBase64Url(response.authenticatorData),
        clientDataJSON: arrayBufferToBase64Url(response.clientDataJSON),
        signature: arrayBufferToBase64Url(response.signature),
        userHandle: response.userHandle
          ? arrayBufferToBase64Url(response.userHandle)
          : undefined,
      },
      type: credential.type,
    };

    // 5. 发送到后端完成认证
    const result = await PasskeyService.finishAuthentication(credentialJson);

    return result.token;
  }
}
```

---

## 4. 自定义 Hook

### 文件: `src/hooks/usePasskey.ts`

```typescript
'use client';

import { useState } from 'react';
import { WebAuthnClient } from '@/services/passkey.service';
import { isWebAuthnSupported } from '@/lib/passkeys/utils';

export interface UsePasskeyResult {
  isSupported: boolean;
  isLoading: boolean;
  error: string | null;
  register: (username: string, displayName: string) => Promise<void>;
  authenticate: (username: string) => Promise<string>;
  clearError: () => void;
}

/**
 * Passkey Hook
 */
export function usePasskey(): UsePasskeyResult {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isSupported = isWebAuthnSupported();

  /**
   * 注册 Passkey
   */
  const register = async (username: string, displayName: string) => {
    if (!isSupported) {
      setError('您的浏览器不支持 Passkeys，请使用最新版 Chrome/Safari/Edge');
      throw new Error('WebAuthn not supported');
    }

    setIsLoading(true);
    setError(null);

    try {
      await WebAuthnClient.register(username, displayName);
    } catch (err: any) {
      const message = err.message || '注册失败，请重试';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * 使用 Passkey 登录
   */
  const authenticate = async (username: string): Promise<string> => {
    if (!isSupported) {
      setError('您的浏览器不支持 Passkeys');
      throw new Error('WebAuthn not supported');
    }

    setIsLoading(true);
    setError(null);

    try {
      const token = await WebAuthnClient.authenticate(username);
      return token;
    } catch (err: any) {
      const message = err.message || '登录失败，请重试';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const clearError = () => setError(null);

  return {
    isSupported,
    isLoading,
    error,
    register,
    authenticate,
    clearError,
  };
}
```

---

## 5. 登录页面集成

### 文件: `src/app/login/page.tsx`

```typescript
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { usePasskey } from '@/hooks/usePasskey';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const { isSupported, isLoading, error, authenticate } = usePasskey();

  /**
   * 使用 Passkey 登录
   */
  const handlePasskeyLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!email) {
      alert('请输入邮箱');
      return;
    }

    try {
      const token = await authenticate(email);

      // 保存 token
      localStorage.setItem('token', token);

      // 跳转到首页
      router.push('/');
    } catch (err) {
      // 错误已在 hook 中处理
      console.error('登录失败:', err);
    }
  };

  /**
   * 传统密码登录（降级方案）
   */
  const handlePasswordLogin = async () => {
    // TODO: 实现密码登录逻辑
    alert('密码登录（待实现）');
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-lg">
        <h1 className="mb-6 text-center text-2xl font-bold">登录</h1>

        <form onSubmit={handlePasskeyLogin} className="space-y-4">
          {/* 邮箱输入 */}
          <div>
            <label className="block text-sm font-medium text-gray-700">
              邮箱
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
              placeholder="your@email.com"
              required
            />
          </div>

          {/* 错误提示 */}
          {error && (
            <div className="rounded-md bg-red-50 p-3 text-sm text-red-600">
              {error}
            </div>
          )}

          {/* Passkey 登录按钮 */}
          {isSupported && (
            <button
              type="submit"
              disabled={isLoading}
              className="w-full rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:bg-gray-400"
            >
              {isLoading ? (
                <span className="flex items-center justify-center">
                  <svg className="mr-2 h-5 w-5 animate-spin" viewBox="0 0 24 24">
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                      fill="none"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                    />
                  </svg>
                  认证中...
                </span>
              ) : (
                <span className="flex items-center justify-center">
                  🔐 使用 Passkey 登录
                </span>
              )}
            </button>
          )}

          {/* 不支持提示 */}
          {!isSupported && (
            <div className="rounded-md bg-yellow-50 p-3 text-sm text-yellow-700">
              您的浏览器不支持 Passkeys，请使用密码登录
            </div>
          )}

          {/* 分割线 */}
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300" />
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="bg-white px-2 text-gray-500">或</span>
            </div>
          </div>

          {/* 密码登录按钮 */}
          <button
            type="button"
            onClick={handlePasswordLogin}
            className="w-full rounded-md border border-gray-300 px-4 py-2 hover:bg-gray-50"
          >
            使用密码登录
          </button>
        </form>

        {/* 注册链接 */}
        <p className="mt-4 text-center text-sm text-gray-600">
          还没有账号？{' '}
          <a href="/register" className="text-blue-600 hover:underline">
            立即注册
          </a>
        </p>
      </div>
    </div>
  );
}
```

---

## 6. 注册页面集成

### 文件: `src/app/register/page.tsx`

```typescript
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { usePasskey } from '@/hooks/usePasskey';

export default function RegisterPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    email: '',
    displayName: '',
    password: '', // 如果需要密码降级方案
  });

  const { isSupported, isLoading, error, register } = usePasskey();

  /**
   * 注册账号并创建 Passkey
   */
  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();

    const { email, displayName } = formData;

    if (!email || !displayName) {
      alert('请填写完整信息');
      return;
    }

    try {
      // Step 1: 先调用后端注册账号（如果还没注册）
      // TODO: await registerAccount(email, password);

      // Step 2: 注册 Passkey
      await register(email, displayName);

      alert('注册成功！请登录');
      router.push('/login');
    } catch (err) {
      console.error('注册失败:', err);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-lg">
        <h1 className="mb-6 text-center text-2xl font-bold">注册账号</h1>

        <form onSubmit={handleRegister} className="space-y-4">
          {/* 邮箱 */}
          <div>
            <label className="block text-sm font-medium text-gray-700">
              邮箱
            </label>
            <input
              type="email"
              value={formData.email}
              onChange={(e) =>
                setFormData({ ...formData, email: e.target.value })
              }
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
              placeholder="your@email.com"
              required
            />
          </div>

          {/* 显示名称 */}
          <div>
            <label className="block text-sm font-medium text-gray-700">
              显示名称
            </label>
            <input
              type="text"
              value={formData.displayName}
              onChange={(e) =>
                setFormData({ ...formData, displayName: e.target.value })
              }
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
              placeholder="张三"
              required
            />
          </div>

          {/* Passkey 提示 */}
          {isSupported && (
            <div className="rounded-md bg-blue-50 p-3 text-sm text-blue-700">
              ✨ 注册后将使用 Passkey（生物识别）登录，无需记忆密码
            </div>
          )}

          {/* 错误提示 */}
          {error && (
            <div className="rounded-md bg-red-50 p-3 text-sm text-red-600">
              {error}
            </div>
          )}

          {/* 注册按钮 */}
          <button
            type="submit"
            disabled={isLoading}
            className="w-full rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:bg-gray-400"
          >
            {isLoading ? '注册中...' : '注册并创建 Passkey'}
          </button>
        </form>

        {/* 登录链接 */}
        <p className="mt-4 text-center text-sm text-gray-600">
          已有账号？{' '}
          <a href="/login" className="text-blue-600 hover:underline">
            立即登录
          </a>
        </p>
      </div>
    </div>
  );
}
```

---

## 7. 用户设置页面（管理 Passkey）

### 文件: `src/app/settings/passkey/page.tsx`

```typescript
'use client';

import { useState, useEffect } from 'react';
import { usePasskey } from '@/hooks/usePasskey';

export default function PasskeySettingsPage() {
  const [hasPasskey, setHasPasskey] = useState(false);
  const [userEmail, setUserEmail] = useState('');
  const { isSupported, isLoading, error, register } = usePasskey();

  useEffect(() => {
    // TODO: 从后端检查用户是否已注册 Passkey
    // const checkPasskey = async () => {
    //   const result = await fetch('/api/passkey/check');
    //   setHasPasskey(result.exists);
    // };
    // checkPasskey();
  }, []);

  const handleCreatePasskey = async () => {
    if (!userEmail) {
      alert('无法获取用户信息');
      return;
    }

    try {
      await register(userEmail, '用户名');
      setHasPasskey(true);
      alert('Passkey 创建成功！');
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="mx-auto max-w-2xl p-6">
      <h1 className="mb-6 text-2xl font-bold">Passkey 设置</h1>

      {!isSupported && (
        <div className="rounded-md bg-yellow-50 p-4 text-yellow-700">
          您的浏览器不支持 Passkeys
        </div>
      )}

      {isSupported && (
        <div className="space-y-4">
          {hasPasskey ? (
            <div className="rounded-md border border-green-200 bg-green-50 p-4">
              <p className="text-green-700">✅ 您已设置 Passkey</p>
              <button className="mt-2 rounded bg-red-500 px-4 py-2 text-white hover:bg-red-600">
                删除 Passkey
              </button>
            </div>
          ) : (
            <div className="rounded-md border border-gray-200 p-4">
              <p className="mb-4 text-gray-700">
                创建 Passkey 后，您可以使用指纹、面容或设备 PIN 快速登录
              </p>
              <button
                onClick={handleCreatePasskey}
                disabled={isLoading}
                className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:bg-gray-400"
              >
                {isLoading ? '创建中...' : '创建 Passkey'}
              </button>
            </div>
          )}

          {error && (
            <div className="rounded-md bg-red-50 p-3 text-red-600">
              {error}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
```

---

## 8. 错误处理

### 常见错误及处理方案

```typescript
export function handleWebAuthnError(error: any): string {
  const errorName = error.name || '';

  switch (errorName) {
    case 'NotAllowedError':
      return '操作被取消或超时，请重试';

    case 'InvalidStateError':
      return '您已注册过 Passkey';

    case 'NotSupportedError':
      return '您的设备不支持此操作';

    case 'SecurityError':
      return '安全错误，请确保使用 HTTPS';

    case 'AbortError':
      return '操作被中止';

    case 'ConstraintError':
      return '设备不满足安全要求';

    default:
      return error.message || '操作失败，请重试';
  }
}
```

在 Hook 中使用：

```typescript
import { handleWebAuthnError } from '@/lib/passkeys/errors';

try {
  await WebAuthnClient.register(username, displayName);
} catch (err: any) {
  const message = handleWebAuthnError(err);
  setError(message);
  throw err;
}
```

---

## 9. TypeScript 类型定义

### 文件: `src/lib/passkeys/types.ts`

```typescript
/**
 * 注册选项（从后端获取）
 */
export interface RegistrationOptions {
  challenge: string; // Base64URL
  rp: {
    name: string;
    id: string;
  };
  user: {
    id: string; // Base64URL
    name: string;
    displayName: string;
  };
  pubKeyCredParams: Array<{
    type: string;
    alg: number;
  }>;
  timeout?: number;
  excludeCredentials?: Array<{
    id: string; // Base64URL
    type: string;
    transports?: string[];
  }>;
  authenticatorSelection?: {
    authenticatorAttachment?: 'platform' | 'cross-platform';
    requireResidentKey?: boolean;
    residentKey?: 'discouraged' | 'preferred' | 'required';
    userVerification?: 'required' | 'preferred' | 'discouraged';
  };
  attestation?: 'none' | 'indirect' | 'direct' | 'enterprise';
}

/**
 * 认证选项（从后端获取）
 */
export interface AuthenticationOptions {
  challenge: string; // Base64URL
  timeout?: number;
  rpId?: string;
  allowCredentials?: Array<{
    id: string; // Base64URL
    type: string;
    transports?: string[];
  }>;
  userVerification?: 'required' | 'preferred' | 'discouraged';
}

/**
 * 凭证响应（发送到后端）
 */
export interface CredentialResponse {
  id: string;
  rawId: string; // Base64URL
  response: {
    attestationObject?: string; // Base64URL (注册时)
    authenticatorData?: string; // Base64URL (认证时)
    clientDataJSON: string; // Base64URL
    signature?: string; // Base64URL (认证时)
    userHandle?: string; // Base64URL (认证时)
  };
  type: string;
}
```

---

## 10. 环境变量配置

### 文件: `.env.local`

```bash
# 后端 API 地址
NEXT_PUBLIC_API_URL=http://localhost:9966

# 生产环境
# NEXT_PUBLIC_API_URL=https://api.yourforum.com
```

---

## 11. 完整使用流程示例

### 场景 1: 新用户注册

```typescript
// 1. 用户填写表单
const formData = {
  email: 'user@example.com',
  displayName: '张三',
};

// 2. 调用后端注册账号（如果需要）
await fetch('/api/auth/register', {
  method: 'POST',
  body: JSON.stringify(formData),
});

// 3. 创建 Passkey
const { register } = usePasskey();
await register(formData.email, formData.displayName);

// 4. 完成！
```

### 场景 2: 用户登录

```typescript
// 1. 用户输入邮箱
const email = 'user@example.com';

// 2. 使用 Passkey 认证
const { authenticate } = usePasskey();
const token = await authenticate(email);

// 3. 保存 token 并跳转
localStorage.setItem('token', token);
router.push('/home');
```

---

## 12. 注意事项

### ⚠️ 生产环境部署

1. **必须使用 HTTPS**
   ```typescript
   // 检测环境
   if (process.env.NODE_ENV === 'production' && !window.location.protocol.startsWith('https')) {
     console.error('Passkeys 需要 HTTPS 环境');
   }
   ```

2. **更新后端配置**
   ```yaml
   # application.yml
   webauthn:
     rp:
       id: yourforum.com  # 实际域名
       origins: https://yourforum.com
   ```

3. **配置 CORS**（如果前后端不同域）
   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public CorsFilter corsFilter() {
           CorsConfiguration config = new CorsConfiguration();
           config.setAllowedOrigins(List.of("https://yourforum.com"));
           config.setAllowedMethods(List.of("*"));
           config.setAllowCredentials(true);
           // ...
       }
   }
   ```

### 📱 兼容性检测

```typescript
useEffect(() => {
  if (!isWebAuthnSupported()) {
    console.warn('浏览器不支持 WebAuthn');
    // 显示降级提示
  }
}, []);
```

### 🔒 安全建议

1. **Token 存储**：使用 `httpOnly` Cookie 代替 `localStorage`
2. **CSRF 防护**：添加 CSRF Token
3. **Rate Limiting**：限制认证尝试次数

---

## 13. 常见问题 (FAQ)

### Q: Next.js SSR 环境下如何使用？
**A**: WebAuthn API 只在客户端可用，所以：
```typescript
'use client'; // 必须标记为客户端组件

useEffect(() => {
  if (typeof window !== 'undefined') {
    // 仅在客户端执行
  }
}, []);
```

### Q: 如何实现自动填充（Autofill UI）？
**A**: 使用条件 UI（Chrome/Safari 支持）：
```typescript
const credential = await navigator.credentials.get({
  publicKey: options,
  mediation: 'conditional', // 启用自动填充
});
```

### Q: 支持哪些浏览器？
**A**:
- ✅ Chrome 67+ (Windows Hello, Touch ID)
- ✅ Safari 14+ (Face ID, Touch ID)
- ✅ Edge 18+ (Windows Hello)
- ✅ Firefox 60+ (需手动启用)
- ❌ IE 不支持

---

## 总结

完成上述步骤后，你的 Next.js 前端将具备：
- ✅ Passkey 注册功能
- ✅ Passkey 登录功能
- ✅ 错误处理和用户提示
- ✅ TypeScript 类型安全
- ✅ 可复用的 Hook 和工具函数

**开始使用**：
1. 复制代码到对应文件
2. 安装依赖（无需额外依赖，Next.js 自带所需功能）
3. 配置环境变量
4. 启动开发服务器测试

有任何问题随时问我！🚀
