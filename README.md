# 캐퍼시터 GPS 플러그인  

Android, iOS 의 GPS 기능을 네이티브로 구현한 플러그인

## Install

```bash
npm install capacitor-geolocation-exmaple-plugin
npx cap sync
```

## API 목록

<docgen-index>

* [`requestPermission()`](#requestpermission)
* [`requestPsermissionMap()`](#requestpsermissionmap)
* [`gpsStart()`](#gpsstart)
* [`gpsStop()`](#gpsstop)
* [`openGoogleMap()`](#opengooglemap)
* [`addListener('abnormal_vitalSign', ...)`](#addlistenerabnormal_vitalsign)
</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### requestPermission()

```typescript
requestPermission() => Promise<{ result: String; }>
```

**Returns:** <code>Promise&lt;{ result: <a href="#string">String</a>; }&gt;</code>

--------------------


### requestPsermissionMap()

```typescript
requestPsermissionMap() => Promise<{ result: String; }>
```

**Returns:** <code>Promise&lt;{ result: <a href="#string">String</a>; }&gt;</code>

--------------------


### gpsStart()

```typescript
gpsStart() => Promise<{ result: String; }>
```

**Returns:** <code>Promise&lt;{ result: <a href="#string">String</a>; }&gt;</code>

--------------------


### gpsStop()

```typescript
gpsStop() => Promise<{ result: String; }>
```

**Returns:** <code>Promise&lt;{ result: <a href="#string">String</a>; }&gt;</code>

--------------------


### openGoogleMap()

```typescript
openGoogleMap() => Promise<{ result: String; }>
```

**Returns:** <code>Promise&lt;{ result: <a href="#string">String</a>; }&gt;</code>

--------------------


### addListener('abnormal_vitalSign', ...)

```typescript
addListener(eventName: 'abnormal_vitalSign', listenerFunc: (value: any) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                 |
| ------------------ | ------------------------------------ |
| **`eventName`**    | <code>'abnormal_vitalSign'</code>    |
| **`listenerFunc`** | <code>(value: any) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


</docgen-api>
