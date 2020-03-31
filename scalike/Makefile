build: scalike.docset

scalike.docset: scalikejdbc.org
				dashing build --config dashing.json --source ./scalikejdbc.org/

scalikejdbc.org:
				wget --mirror --convert-links --adjust-extension --page-requisites --no-parent -X documentation/1.x,documentation/2.x http://scalikejdbc.org/

clean:
				rm -rf scalike.docset scalikejdbc.org

.PHONY: build clean
