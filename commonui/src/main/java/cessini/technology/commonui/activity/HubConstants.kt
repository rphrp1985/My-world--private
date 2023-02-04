package cessini.technology.commonui.activity

object HubConstants {
    val awsconfig = """
  {
  "UserAgent": "MobileHub/1.0",
  "Version": "1.0",
  "CredentialsProvider": {
"CognitoIdentity": {
  "Default": {
    "PoolId": "ap-south-1:1ef82df2-73d2-4714-a978-5afbf42dc816",
    "Region": "ap-south-1"
  }
}
  },
  "IdentityManager": {
    "Default": {}
  },
  "S3TransferUtility": {
    "Default": {
      "Bucket": "kurento-recorded-file",
      "Region": "ap-south-1"
    }
  }
}"""
    val amplifyconfig = """
  {
    "auth": {
        "plugins": {
            "awsCognitoAuthPlugin": {
                "IdentityManager": {
                    "Default": {}
                },
                "CredentialsProvider": {
                    "CognitoIdentity": {
                        "Default": {
                            "PoolId": "ap-south-1:1ef82df2-73d2-4714-a978-5afbf42dc816",
                            "Region": "ap-south-1"
                        }
                    }
                },
                "CognitoUserPool": {
                    "Default": {
                        "PoolId": "ap-south-1_tdZpslecO",
                        "AppClientId": "64frfm3gdkcnhij3amj9750dq7",
                        "Region": "ap-south-1"
                    }
                }
                
            }
        }
    },
    "storage": {
        "plugins": {
            "awsS3StoragePlugin": {
                  "bucket": "kurento-recorded-file",
                  "region": "ap-south-1"
            }
        }
    }
}"""


    val key = "AKIASGPFZLBMV25FCLWJ"
    val secret = "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"
    val maxerror = 5000
    val maxtimeout = 36000

}