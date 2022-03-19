package dreifa.app.tasks.ui.adapters

import dreifa.app.fileBundle.BinaryBundleItem
import dreifa.app.fileBundle.FileBundle
import dreifa.app.fileBundle.builders.FileBundleBuilder
import org.http4k.core.MultipartFormBody
import org.http4k.core.Request
import java.io.File

class MulitPartRequestToFileBundleAdapter {
    fun toFileBundle(request: Request): FileBundle {
        val receivedForm = MultipartFormBody.from(request)
        val multipart = receivedForm.files("payload")[0]
        val f = File(multipart.filename)
        f.writeBytes(multipart.content.readAllBytes())

        return FileBundleBuilder()
            .withName("${multipart.filename} Bundle")
            .addItem(BinaryBundleItem.fromFile(f, multipart.filename))
            .build()
    }
}