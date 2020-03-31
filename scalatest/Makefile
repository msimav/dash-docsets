build: scalatest.docset icon

scalatest.docset: www.scalatest.org
				dashing build --config dashing.json --source ./www.scalatest.org/

icon:
				wget https://pbs.twimg.com/profile_images/2426141793/46gxfdkx9gcwbssmjfao_reasonably_small.gif -O icon.gif
				magick icon.gif -resize 32x32 scalatest.docset/icon@2x.png
				magick icon.gif -resize 16x16 scalatest.docset/icon.png
				rm -f icon.gif

www.scalatest.org:
				wget --mirror --convert-links --adjust-extension --page-requisites --no-parent -I user_guide,assets http://www.scalatest.org/user_guide

clean:
				rm -rf scalatest.docset www.scalatest.org

.PHONY: build icon clean
