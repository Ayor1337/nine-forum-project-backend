package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Passkey options 的统一返回包装。
 *
 * @param <T> publicKey 的具体类型
 */
public class PasskeyOptionsVO<T> {

    private String requestId;

    private T publicKey;
}
