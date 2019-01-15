package pl.swd.app.services.DecisionTree

import pl.swd.app.models.DataColumn

class TreeNode(decisionColumn: DataColumn, attributeColums: List<DataColumn>, nodeValue: Int = -1) {
    var parent: TreeNode? = null
    var childrens: ArrayList<TreeNode> = ArrayList()

    var attributeName: String = ""
    var decisionColumn: DataColumn
    var attributeColums: List<DataColumn>

    var nodeValue: Int
    var decisionClassAtr: Int = -1

    var visited = false
    var decisionClass = false

    init {
        this.decisionColumn = decisionColumn
        this.attributeColums = attributeColums
        this.nodeValue = nodeValue
    }
}