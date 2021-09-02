 <declare-styleable name="MjpegSurfaceView">
        <attr name="type"/>
        <attr name="transparentBackground" format="boolean" />
        <attr name="backgroundColor" format="color" />
    </declare-styleable>

    <attr name="type" format="enum">
        <enum name="stream_default" value="0"/>
        <enum name="stream_native" value="1"/>
    </attr>