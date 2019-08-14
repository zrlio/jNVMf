package com.ibm.jnvmf;

public class StatusField extends Updatable<NativeBuffer> {
  /*
   * NVMe Spec 1.3a - 4.6.1
   *
   *  01:08 Status Code
   *  09:11 Status Code Type
   *  12:13 Reserved
   *  14    More
   *  15    Do not retry
   *
   */

  private final int offset;

  private static final int STATUS_CODE_OFFSET = 0;
  private static final int STATUS_CODE_BITOFFSET_START = 1;
  private static final int STATUS_CODE_BITOFFSET_END = 8;
  private int rawStatusCode;

  private static final int STATUS_CODE_TYPE_OFFSET = 1;
  private static final int STATUS_CODE_TYPE_BITOFFSET_START = 1;
  private static final int STATUS_CODE_TYPE_BITOFFSET_END = 3;
  private StatusCodeType.Value statusCodeType;

  private static final int MORE_OFFSET = 1;
  private static final int MORE_BITOFFSET = 6;
  private boolean more;

  private static final int DO_NOT_RETRY_OFFSET = 1;
  private static final int DO_NOT_RETRY_BITOFFSET = 7;
  private boolean doNotRetry;

  StatusField(int offset) {
    this.offset = offset;
  }

  public final StatusCodeType.Value getStatusCodeType() {
    return statusCodeType;
  }

  final StatusCode.Value getAdminStatusCode() {
    return getStatusCodeType().adminValueOf(rawStatusCode);
  }

  final StatusCode.Value getFabricsStatusCode() {
    return getStatusCodeType().fabricsValueOf(rawStatusCode);
  }

  final StatusCode.Value getNvmStatusCode() {
    return getStatusCodeType().nvmValueOf(rawStatusCode);
  }

  public final boolean getMore() {
    return more;
  }

  public final boolean getDoNotRetry() {
    return doNotRetry;
  }

  @Override
  void update(NativeBuffer buffer) {
    int b1 = buffer.get(offset + STATUS_CODE_OFFSET);
    rawStatusCode = BitUtil.getBits(b1, STATUS_CODE_BITOFFSET_START, STATUS_CODE_BITOFFSET_END);
    int b2 = buffer.get(offset + STATUS_CODE_TYPE_OFFSET);
    statusCodeType =
        StatusCodeType.getInstance().valueOf(BitUtil.getBits(b2, STATUS_CODE_TYPE_BITOFFSET_START,
        STATUS_CODE_TYPE_BITOFFSET_END));
    assert (MORE_OFFSET == STATUS_CODE_TYPE_OFFSET);
    more = BitUtil.getBit(b1, MORE_BITOFFSET);
    assert (DO_NOT_RETRY_OFFSET == STATUS_CODE_TYPE_OFFSET);
    doNotRetry = BitUtil.getBit(b1, DO_NOT_RETRY_BITOFFSET);
  }
}
