package com.github.microwww.bitcoin.conf;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.chain.Generating;
import com.github.microwww.bitcoin.math.Uint32;
import org.springframework.stereotype.Component;

@Component
public class CChainParams {
    public final Settings settings;
    public final Env env;

    public CChainParams(Settings settings) {
        this.settings = settings;
        this.env = settings.getEnv();
    }

    public Params getEnvParams() {
        return env.params;
    }

    public enum Env {
        MAIN() {
            @Override
            public ChainBlock createGenesisBlock() {
                return Generating.createGenesisBlock(new Uint32(1231006505), new Uint32(2083236893), new Uint32(0x1d00ffff), 1, 50 * Generating.COIN);
            }

            @Override
            void init() {
                this.params.dataDirPrefix = "/";
                params.magic = 0xf9beb4d9;
            }
        }, TEST() {
            @Override
            public ChainBlock createGenesisBlock() {
                return Generating.createGenesisBlock(new Uint32(1296688602), new Uint32(414098458), new Uint32(0x1d00ffff), 1, 50 * Generating.COIN);
            }

            @Override
            void init() {
                params.dataDirPrefix = "/test";
                params.magic = 0x0b110907;
            }
        }, REG_TEST() {
            @Override
            public ChainBlock createGenesisBlock() {
                return Generating.createGenesisBlock(new Uint32(1296688602), new Uint32(2), new Uint32(0x207fffff), 1, 50 * Generating.COIN);
            }

            @Override
            void init() {
                params.dataDirPrefix = "/regtest";
                params.magic = 0xfabfb5da;
            }
        };
        public final Params params = new Params();

        Env() {
            init();
        }

        public abstract ChainBlock createGenesisBlock();

        abstract void init();
    }

    public static class Params {
        private String dataDirPrefix;
        private int magic = 0xfabfb5da; // 0xf9beb4d9;

    /*
    strNetworkID =  CBaseChainParams::REGTEST;
    consensus.signet_blocks = false;
    consensus.signet_challenge.clear();
    consensus.nSubsidyHalvingInterval = 150;
    consensus.BIP16Exception = uint256();
    consensus.BIP34Height = 500; // BIP34 activated on regtest (Used in functional tests)
    consensus.BIP34Hash = uint256();
    consensus.BIP65Height = 1351; // BIP65 activated on regtest (Used in functional tests)
    consensus.BIP66Height = 1251; // BIP66 activated on regtest (Used in functional tests)
    consensus.CSVHeight = 432; // CSV activated on regtest (Used in rpc activation tests)
    consensus.SegwitHeight = 0; // SEGWIT is always activated on regtest unless overridden
    consensus.MinBIP9WarningHeight = 0;
    consensus.powLimit = uint256S("7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
    consensus.nPowTargetTimespan = 14 * 24 * 60 * 60; // two weeks
    consensus.nPowTargetSpacing = 10 * 60;
    consensus.fPowAllowMinDifficultyBlocks = true;
    consensus.fPowNoRetargeting = true;
    consensus.nRuleChangeActivationThreshold = 108; // 75% for testchains
    consensus.nMinerConfirmationWindow = 144; // Faster than normal for regtest (144 instead of 2016)

    consensus.vDeployments[Consensus::DEPLOYMENT_TESTDUMMY].bit = 28;
    consensus.vDeployments[Consensus::DEPLOYMENT_TESTDUMMY].nStartTime = 0;
    consensus.vDeployments[Consensus::DEPLOYMENT_TESTDUMMY].nTimeout = Consensus::BIP9Deployment::NO_TIMEOUT;
    consensus.vDeployments[Consensus::DEPLOYMENT_TESTDUMMY].min_activation_height = 0; // No activation delay

    consensus.vDeployments[Consensus::DEPLOYMENT_TAPROOT].bit = 2;
    consensus.vDeployments[Consensus::DEPLOYMENT_TAPROOT].nStartTime = Consensus::BIP9Deployment::ALWAYS_ACTIVE;
    consensus.vDeployments[Consensus::DEPLOYMENT_TAPROOT].nTimeout = Consensus::BIP9Deployment::NO_TIMEOUT;
    consensus.vDeployments[Consensus::DEPLOYMENT_TAPROOT].min_activation_height = 0; // No activation delay

    consensus.nMinimumChainWork = uint256{};
    consensus.defaultAssumeValid = uint256{};

    pchMessageStart[0] = 0xfa;
    pchMessageStart[1] = 0xbf;
    pchMessageStart[2] = 0xb5;
    pchMessageStart[3] = 0xda;
    nDefaultPort = 18444;
    nPruneAfterHeight = 1000;
    m_assumed_blockchain_size = 0;
    m_assumed_chain_state_size = 0;

    UpdateActivationParametersFromArgs(args);

    genesis = CreateGenesisBlock(1296688602, 2, 0x207fffff, 1, 50 * COIN);
    consensus.hashGenesisBlock = genesis.GetHash();
        assert(consensus.hashGenesisBlock == uint256S("0x0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206"));
        assert(genesis.hashMerkleRoot == uint256S("0x4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"));

        vFixedSeeds.clear(); //!< Regtest mode doesn't have any fixed seeds.
        vSeeds.clear();      //!< Regtest mode doesn't have any DNS seeds.

    fDefaultConsistencyChecks = true;
    fRequireStandard = true;
    m_is_test_chain = true;
    m_is_mockable_chain = true;

    checkpointData = {
        {
            {0, uint256S("0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206")},
        }
    };

    chainTxData = ChainTxData{
        0,
                0,
                0
    };

    base58Prefixes[PUBKEY_ADDRESS] = std::vector<unsigned char>(1,111);
    base58Prefixes[SCRIPT_ADDRESS] = std::vector<unsigned char>(1,196);
    base58Prefixes[SECRET_KEY] =     std::vector<unsigned char>(1,239);
    base58Prefixes[EXT_PUBLIC_KEY] = {0x04, 0x35, 0x87, 0xCF};
    base58Prefixes[EXT_SECRET_KEY] = {0x04, 0x35, 0x83, 0x94};

    bech32_hrp = "bcrt";
     */

        public String getDataDirPrefix() {
            return dataDirPrefix;
        }

        public int getMagic() {
            return magic;
        }
    }
}
