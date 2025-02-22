Experimental Settings
=====================

<figure align="center" class="image">
  <img src="_static/images/settings/experimental/settings_experimental_framed.png" width="350px"> 
  <figcaption><i>Experimental settings screen layout</i></figcaption> 
</figure>

Stable
------

#### <img ref="flask" style="vertical-align: middle;" src="_static/icons/settings/experimental/flask-outline.png" width="20px"> Repeated Measures

Turns on repeated measures. When turned on, a green plus symbol appears
next to the trait value entry box on the collect screen.

<figure align="center" class="image">
  <img src="_static/images/settings/experimental/settings_experimental_repeated_icon.png" width="325px"> 
  <figcaption><i>Collect screen value entry with repeated measurements enabled</i></figcaption> 
</figure>

When pressed it creates a new entry field for collecting an additional
observation on the same plot for the same trait.

!> To export data that includes repeated measures make sure to choose the
**Database** format or to use **BrAPI**. These formats allow repeated
measures to be differentiated by timestamp. If exporting in **Table**
format then only the latest measurement will be included.

Beta
----

#### <img ref="audio" style="vertical-align: middle;" src="_static/icons/settings/experimental/microphone-message.png" width="20px"> Enable Field Audio

Adds icon to collect toolbar for recording audio at the field level.

#### <img ref="library" style="vertical-align: middle;" src="_static/icons/settings/experimental/qrcode-scan.png" width="20px"> MLKit Library

Changes the software library used for barcode scans from ZXing to MLKit.

Alpha
-----

#### <img ref="brapi" style="vertical-align: middle;" src="_static/icons/settings/experimental/server.png" width="20px"> New BrAPI Import UI

Replace old BrAPI field and trait import UIs with new streamlined version that includes search and additional step for identifer/format selection.

#### <img ref="media" style="vertical-align: middle;" src="_static/icons/settings/experimental/gamepad.png" width="20px"> Enable Media Commands

Allows media remotes to control app through media commands.