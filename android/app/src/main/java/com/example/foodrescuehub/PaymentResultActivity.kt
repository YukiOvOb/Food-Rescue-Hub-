class PaymentResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_result)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnHome = findViewById<Button>(R.id.btnHome)

        val data: Uri? = intent.data

        if (data != null && data.toString().contains("success")) {
            //successful
            val orderIds = data.getQueryParameter("order_ids") // get order id "101,102"

            tvStatus.text = "Payment Successful!\nOrders: $orderIds"
            tvStatus.setTextColor(Color.GREEN)

            // confirm the status of order (Double Check)

        } else if (data != null && data.toString().contains("cancel")) {
            // if cancel
            tvStatus.text = "Payment Cancelled"
            tvStatus.setTextColor(Color.RED)
        }

        btnHome.setOnClickListener {
            // back to home page
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}