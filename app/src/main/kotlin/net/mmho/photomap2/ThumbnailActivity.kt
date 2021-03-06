package net.mmho.photomap2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_thumbnail_list.*

class ThumbnailActivity : AppCompatActivity() {
    private var fragment: ThumbnailFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thumbnail_list)

        when (val bundle = intent.extras) {
            null -> {
                finish()
            }
            else -> {
                fragment = supportFragmentManager.findFragmentById(R.id.list) as ThumbnailFragment
                fragment?.setList(bundle.getParcelable(EXTRA_GROUP))
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    companion object {
        const val EXTRA_GROUP = "thumbnail_group"
    }
}
