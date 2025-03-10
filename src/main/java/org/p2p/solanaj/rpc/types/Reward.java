package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;

public class Reward {

    @Json(name = "pubkey")
    private String pubkey;

    @Json(name = "lamports")
    private int lamports;

    @Json(name = "postBalance")
    private String postBalance;

    @Json(name = "rewardType")
    private RewardType rewardType;
}
