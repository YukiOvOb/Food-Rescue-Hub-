
data class CheckoutRequest(
    val userId: Long,
    val items: List<CheckoutItem>
)

data class CheckoutItem(
    val listingId: Long,
    val quantity: Int
)


data class CheckoutResponse(
    val paymentUrl: String,
    val orderIds: List<Long>
)