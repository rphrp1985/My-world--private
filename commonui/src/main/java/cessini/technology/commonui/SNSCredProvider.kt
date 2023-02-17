package cessini.technology.commonui

import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.time.Instant

object AmazonModule : CredentialsProvider {
    override suspend fun getCredentials(): Credentials {
        return Credentials("AKIAXT3BE4LD5PRL3FPH", "OKgUFQFGPy700m8tSNqfGOW4/MqjcHtMp8cuaewJ")
    }
}
